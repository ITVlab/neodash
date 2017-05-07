package news.androidtv.neodash.activities;

import android.app.WallpaperManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.apps.muzei.MuzeiActivity;
import com.google.android.apps.muzei.MuzeiWallpaperService;
import com.google.android.apps.muzei.sync.DownloadArtworkJobService;

/**
 * Created by Nick on 5/6/2017.
 */

public class StartupActivity extends MuzeiActivity {
    private static final int LOAD_ARTWORK_JOB_ID = 1;

    @Override
    protected void setupIntroModeUi() {
        ViewGroup mIntroContainerView = (ViewGroup) findViewById(net.nurik.roman.muzei.R.id.intro_container);

        findViewById(net.nurik.roman.muzei.R.id.activate_muzei_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start scheduling stuff.
                // Open up some sort of selection UI.
                // Settings already seem to be dpad-able. Just needs
            }
        });
    }

    protected void startJobService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(new JobInfo.Builder(LOAD_ARTWORK_JOB_ID,
                    new ComponentName(this, DownloadArtworkJobService.class))
                    .setRequiredNetworkType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                            ? JobInfo.NETWORK_TYPE_NOT_ROAMING
                            : JobInfo.NETWORK_TYPE_ANY)
                    .build());
        } else {
            Toast.makeText(this, "This app won't work on your OS version.", Toast.LENGTH_SHORT).show();
        }
    }
}
