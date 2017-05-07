/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.dashclock.weather;

import com.google.android.apps.dashclock.configuration.BaseSettingsActivity;

import net.nurik.roman.dashclock.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class WeatherSettingsActivity extends BaseSettingsActivity {
    @Override
    protected void setupSimplePreferencesScreen() {
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        mFragment.addPreferencesFromResource(R.xml.pref_weather);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(mFragment.findPreference(WeatherExtension.PREF_WEATHER_UNITS));
        bindPreferenceSummaryToValue(mFragment.findPreference(WeatherExtension.PREF_WEATHER_SHORTCUT));
        bindPreferenceSummaryToValue(mFragment.findPreference(WeatherExtension.PREF_WEATHER_LOCATION));
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Clear out the throttle since settings may have changed.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().remove(WeatherExtension.STATE_WEATHER_LAST_UPDATE_ELAPSED_MILLIS).apply();
    }
}
