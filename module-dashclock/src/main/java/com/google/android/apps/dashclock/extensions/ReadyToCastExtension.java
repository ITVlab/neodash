package com.google.android.apps.dashclock.extensions;

import com.google.android.apps.dashclock.LogUtils;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import net.nurik.roman.dashclock.R;

/**
 * Created by Nick on 5/20/2017.
 *
 * An extension which simply says that we are "Ready to Cast".
 */

public class ReadyToCastExtension extends DashClockExtension {
    private static final String TAG = LogUtils.makeLogTag(ReadyToCastExtension.class);

    @Override
    protected void onUpdateData(int reason) {
        publishUpdate(new ExtensionData()
                .visible(true)
                .status("Ready to Cast")
                .icon(R.drawable.ic_cast_white_24dp)
                .clickIntent(null));
    }
}
