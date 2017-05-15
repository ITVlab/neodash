package news.androidtv.neodash.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Nick on 5/14/2017.
 *
 * To simplify the setup process for the app, this class contains static methods which can be
 * called to immediately set the default parameters. These may be different settings than those
 * of the original applications.
 */

public class DefaultSettingsApplier {
    public static void setAllDefaults(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setBackgroundColor(sharedPreferences);
    }

    public static void setBackgroundColor(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // See @ module-dashclock/res/xml/pref_appearance
        editor.putString("pref_homescreen_background_opacity", "50");
        editor.apply();
    }
}
