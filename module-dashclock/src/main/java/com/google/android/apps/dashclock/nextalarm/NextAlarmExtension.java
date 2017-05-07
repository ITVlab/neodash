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

package com.google.android.apps.dashclock.nextalarm;

import com.google.android.apps.dashclock.LogUtils;
import com.google.android.apps.dashclock.Utils;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.google.android.apps.dashclock.configuration.AppChooserPreference;

import net.nurik.roman.dashclock.R;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Next alarm extension.
 */
public class NextAlarmExtension extends DashClockExtension {
    private static final String TAG = LogUtils.makeLogTag(NextAlarmExtension.class);

    public static final String PREF_ALARM_SHORTCUT = "pref_alarm_shortcut";

    private static Pattern sDigitPattern = Pattern.compile("\\s[0-9]");

    private boolean mRegistered = false;
    private BroadcastReceiver mNextAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onUpdateData(0);
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void registerNextAlarmBroadcast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED);
            registerReceiver(mNextAlarmReceiver, filter);
            mRegistered = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void unregisterNextAlarmBroadcast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mRegistered) {
                unregisterReceiver(mNextAlarmReceiver);
                mRegistered = false;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerNextAlarmBroadcast();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterNextAlarmBroadcast();
    }

    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);
        if (!isReconnect && !mRegistered) {
            addWatchContentUris(new String[]{
                    Settings.System.getUriFor(Settings.System.NEXT_ALARM_FORMATTED).toString()
            });
        }
    }

    @Override
    protected void onUpdateData(int reason) {
        String nextAlarm = Settings.System.getString(getContentResolver(),
                Settings.System.NEXT_ALARM_FORMATTED);
        if (!TextUtils.isEmpty(nextAlarm)) {
            Matcher m = sDigitPattern.matcher(nextAlarm);
            if (m.find() && m.start() > 0) {
                nextAlarm = nextAlarm.substring(0, m.start()) + "\n"
                        + nextAlarm.substring(m.start() + 1); // +1 to skip whitespace
            }
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Intent alarmIntent = AppChooserPreference.getIntentValue(
                sp.getString(PREF_ALARM_SHORTCUT, null), null);
        if (alarmIntent == null) {
            alarmIntent = Utils.getDefaultAlarmsIntent(this);
        }

        publishUpdate(new ExtensionData()
                .visible(!TextUtils.isEmpty(nextAlarm))
                .icon(R.drawable.ic_extension_next_alarm)
                .status(nextAlarm)
                .clickIntent(alarmIntent));
    }
}
