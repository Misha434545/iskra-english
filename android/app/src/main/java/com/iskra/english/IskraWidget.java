package com.iskra.english;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/** Віджет: вогник із кількістю днів поспіль. Натискання відкриває застосунок. */
public class IskraWidget extends AppWidgetProvider {
    static final String PREFS = "iskra_widget";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            manager.updateAppWidget(id, build(context));
        }
    }

    static void refreshAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, IskraWidget.class));
        for (int id : ids) {
            manager.updateAppWidget(id, build(context));
        }
    }

    static RemoteViews build(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int streak = p.getInt("streak", 0);
        String last = p.getString("last", "");
        boolean earned = p.getBoolean("earned", false);

        boolean done = false;
        long lastDay = parseDay(last);
        if (lastDay >= 0) {
            long gap = todayDay() - lastDay;
            done = (gap == 0);
            // так само, як у застосунку: пропущений день гасить вогник
            if (gap > 1 && !earned) {
                streak = 0;
            }
        }

        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_flame);
        v.setImageViewResource(R.id.flame, streak > 0 ? R.drawable.ic_flame_on : R.drawable.ic_flame_off);
        v.setTextViewText(R.id.count, String.valueOf(streak));
        v.setTextViewText(R.id.label, dayWord(streak) + " поспіль");
        String status;
        if (done) {
            status = "Сьогодні готово \u2713";
        } else if (earned) {
            status = "Robux чекають!";
        } else {
            status = "Час для уроку!";
        }
        v.setTextViewText(R.id.status, status);

        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.root, pi);
        return v;
    }

    static String dayWord(int n) {
        int a = Math.abs(n) % 100;
        int b = a % 10;
        if (a > 10 && a < 20) return "днів";
        if (b > 1 && b < 5) return "дні";
        if (b == 1) return "день";
        return "днів";
    }

    private static long utcDay(int year, int month0, int day) {
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        c.clear();
        c.set(year, month0, day);
        return c.getTimeInMillis() / 86400000L;
    }

    private static long todayDay() {
        Calendar c = Calendar.getInstance();
        return utcDay(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }

    /** "2026-11-19" -> номер дня, або -1, якщо рядок порожній чи неправильний. */
    private static long parseDay(String s) {
        if (s == null || s.length() != 10) return -1;
        try {
            int y = Integer.parseInt(s.substring(0, 4));
            int m = Integer.parseInt(s.substring(5, 7)) - 1;
            int d = Integer.parseInt(s.substring(8, 10));
            return utcDay(y, m, d);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
