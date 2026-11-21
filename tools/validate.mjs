// Перевірка файлу зі словами. Запуск: node tools/validate.mjs www/words.json
import { readFileSync } from 'node:fs';

const file = process.argv[2] || 'www/words.json';
let data;
try {
  data = JSON.parse(readFileSync(file, 'utf8'));
} catch (e) {
  console.error('✗ Файл не читається як JSON (зайва або пропущена кома, лапка чи дужка):');
  console.error('  ' + e.message);
  process.exit(1);
}

const errors = [];
const warns = [];

function checkWord(w, where) {
  if (!Array.isArray(w) || w.length !== 3 || w.some((x) => typeof x !== 'string' || !x.trim())) {
    errors.push(`${where}: слово має бути у вигляді ["емодзі", "english", "українською"].`);
    return false;
  }
  return true;
}

if (!Array.isArray(data.lessons) || data.lessons.length === 0) {
  errors.push('Немає списку "lessons".');
} else {
  const seenEn = new Map();
  data.lessons.forEach((l, i) => {
    const where = `Урок ${i + 1}${l && l.t ? ` («${l.t}»)` : ''}`;
    if (!l || typeof l.t !== 'string' || !l.t.trim()) errors.push(`${where}: немає назви "t".`);
    if (!l || !Array.isArray(l.w)) {
      errors.push(`${where}: немає списку слів "w".`);
      return;
    }
    if (l.w.length < 3) errors.push(`${where}: потрібно щонайменше 3 слова, зараз ${l.w.length}.`);
    else if (l.w.length !== 4) warns.push(`${where}: слів ${l.w.length}, а зазвичай 4. Працюватиме, але урок буде іншої довжини.`);
    const inLesson = new Set();
    l.w.forEach((w, j) => {
      if (!checkWord(w, `${where}, слово ${j + 1}`)) return;
      const key = w[1].toLowerCase();
      if (inLesson.has(key) || inLesson.has('uk:' + w[2].toLowerCase())) {
        errors.push(`${where}: слово «${w[1]}» повторюється в одному уроці, у питаннях будуть однакові варіанти.`);
      }
      inLesson.add(key);
      inLesson.add('uk:' + w[2].toLowerCase());
      if (seenEn.has(key)) warns.push(`${where}: «${w[1]}» уже було в уроці ${seenEn.get(key)}.`);
      else seenEn.set(key, i + 1);
    });
  });
}

if (data.monthlyTests !== undefined) {
  if (typeof data.monthlyTests !== 'object' || data.monthlyTests === null || Array.isArray(data.monthlyTests)) {
    errors.push('"monthlyTests" має бути об’єктом: { "2026-12": [ слова ] }.');
  } else {
    for (const [month, list] of Object.entries(data.monthlyTests)) {
      if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(month)) errors.push(`monthlyTests: ключ «${month}» має виглядати як 2026-12.`);
      if (!Array.isArray(list)) errors.push(`monthlyTests ${month}: має бути список слів.`);
      else list.forEach((w, j) => checkWord(w, `monthlyTests ${month}, слово ${j + 1}`));
    }
  }
}
if (data.testDay !== undefined && !(Number.isInteger(data.testDay) && data.testDay >= 1 && data.testDay <= 28)) {
  errors.push('"testDay" має бути числом від 1 до 28.');
}
if (data.goalDays !== undefined && !(Number.isInteger(data.goalDays) && data.goalDays >= 1)) {
  errors.push('"goalDays" має бути цілим числом, наприклад 30.');
}

warns.forEach((m) => console.log('! ' + m));
if (errors.length) {
  errors.forEach((m) => console.error('✗ ' + m));
  console.error(`\nЗнайдено помилок: ${errors.length}. Застосунок проігнорує цей файл і залишить попередні слова, поки помилки не буде виправлено.`);
  process.exit(1);
}
console.log(`✓ Усе гаразд: уроків ${data.lessons.length}, слів ${data.lessons.reduce((n, l) => n + l.w.length, 0)}.`);
