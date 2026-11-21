package com.iskra.english;

import android.content.Context;
import android.content.SharedPreferences;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Передає серію вогників із застосунку у віджет на головному екрані. */
@CapacitorPlugin(name = "IskraBridge")
public class IskraBridge extends Plugin {

    @PluginMethod
    public void updateWidget(PluginCall call) {
        Context ctx = getContext();
        SharedPreferences.Editor e = ctx.getSharedPreferences(IskraWidget.PREFS, Context.MODE_PRIVATE).edit();
        e.putInt("streak", call.getInt("streak", 0));
        e.putInt("goal", call.getInt("goal", 30));
        e.putString("last", call.getString("last", ""));
        e.putBoolean("earned", call.getBoolean("earned", false));
        e.apply();
        IskraWidget.refreshAll(ctx);
        call.resolve();
    }
}
