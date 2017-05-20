package news.androidtv.neodash.activities;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.google.android.apps.dashclock.configuration.BaseSettingsActivity;

import news.androidtv.neodash.R;

/**
 * Created by Nick on 5/20/2017.
 */

public class AppSettingsActivity extends BaseSettingsActivity {
    private SharedPreferences mPreferences;

    @Override
    protected void setupSimplePreferencesScreen() {
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        mFragment.addPreferencesFromResource(R.xml.app_prefs);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        updateWifiUi();
        mFragment.findPreference("pref_wifi_enable").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                updateWifiUi();
                return true;
            }
        });
    }

    public void updateWifiUi() {
        if (mPreferences.getBoolean("pref_wifi_enable", false)) {
            mFragment.findPreference("pref_wifi_ssid").setEnabled(true);
            mFragment.findPreference("pref_wifi_password").setEnabled(true);
        } else {
            mFragment.findPreference("pref_wifi_ssid").setEnabled(false);
            mFragment.findPreference("pref_wifi_password").setEnabled(false);
        }
    }
}

