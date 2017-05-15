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

package news.androidtv.neodash.extensions.debug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.apps.dashclock.LogUtils;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import net.nurik.roman.dashclock.R;

import java.util.regex.Pattern;

/**
 * Next alarm extension.
 */
public class DummyExtension extends DashClockExtension {
    private static final String TAG = LogUtils.makeLogTag(DummyExtension.class);

    public static final String PREF_ALARM_SHORTCUT = "pref_alarm_shortcut";

    private static Pattern sDigitPattern = Pattern.compile("\\s[0-9]");

    private boolean mRegistered = false;
    private BroadcastReceiver mNextAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onUpdateData(0);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);
    }

    @Override
    protected void onUpdateData(int reason) {
        publishUpdate(new ExtensionData()
                .visible(true)
                .icon(R.drawable.ic_extension_next_alarm)
                .status("Here you can have some data.")
                .clickIntent(null));
    }
}
