package news.androidtv.neodash.extensions.weather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.Preference;
import android.support.annotation.NonNull;

import com.google.android.apps.dashclock.configuration.BaseSettingsActivity;

import news.androidtv.neodash.R;

public class WeatherSettingsActivity extends BaseSettingsActivity {
    @Override
    protected void setupSimplePreferencesScreen() {
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        mFragment.addPreferencesFromResource(R.xml.weather_prefs);

        setTitle("Weather Extension");

        // Show the current location access.
        updatePermissionUi();
        mFragment.findPreference("pref_weather_location").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        updatePermissionUi();
    }

    public void updatePermissionUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_DENIED) {
            mFragment.findPreference("pref_weather_location").setTitle("Location Access Required");
            mFragment.findPreference("pref_weather_location").setEnabled(true);
        } else {
            mFragment.findPreference("pref_weather_location").setTitle("Location Access Granted");
            mFragment.findPreference("pref_weather_location").setEnabled(false);
        }
    }
}
