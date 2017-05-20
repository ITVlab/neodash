package news.androidtv.neodash.extensions.weather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.android.apps.dashclock.configuration.BaseSettingsActivity;

import net.nurik.roman.dashclock.R;

import news.androidtv.neodash.extensions.debug.DummyExtension;

public class WeatherSettingsActivity extends BaseSettingsActivity {
    @Override
    protected void setupSimplePreferencesScreen() {
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        mFragment.addPreferencesFromResource(R.xml.weather_prefs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_DENIED) {
            mFragment.findPreference(DummyExtension.PREF_ALARM_SHORTCUT).setTitle("Location Access Required");
        } else {
            mFragment.findPreference(DummyExtension.PREF_ALARM_SHORTCUT).setTitle("Location Access Granted");
        }
    }
}
