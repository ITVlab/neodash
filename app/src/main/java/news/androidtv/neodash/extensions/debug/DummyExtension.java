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
                .icon(net.nurik.roman.muzei.R.drawable.ic_ab_done)
                .status(getString(news.androidtv.neodash.R.string.extension_dummy_text))
                .clickIntent(null));
    }
}
