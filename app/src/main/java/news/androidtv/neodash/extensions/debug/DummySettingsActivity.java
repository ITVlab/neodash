package news.androidtv.neodash.extensions.debug;

import com.google.android.apps.dashclock.configuration.BaseSettingsActivity;

import net.nurik.roman.dashclock.R;

public class DummySettingsActivity extends BaseSettingsActivity {
    @Override
    protected void setupSimplePreferencesScreen() {
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        mFragment.addPreferencesFromResource(R.xml.pref_nextalarm);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(
                mFragment.findPreference(DummyExtension.PREF_ALARM_SHORTCUT));
    }
}
