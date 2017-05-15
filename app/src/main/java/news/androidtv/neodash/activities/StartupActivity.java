package news.androidtv.neodash.activities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.apps.muzei.render.MuzeiRendererFragment;
import com.google.android.apps.muzei.sync.DownloadArtworkJobService;

import news.androidtv.neodash.R;
import news.androidtv.neodash.utils.DefaultSettingsApplier;

import static news.androidtv.neodash.Constants.PREF_INIT;

/**
 * Created by Nick on 5/6/2017.
 *
 * The initial activity to be displayed.
 */

public class StartupActivity extends AppCompatActivity {
    private static final int LOAD_ARTWORK_JOB_ID = 1;

    private View.OnFocusChangeListener mFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                v.setBackgroundColor(getResources().getColor(R.color.colorPrimary_50_alpha));
            } else {
                v.setBackgroundColor(getResources().getColor(R.color.colorAccent_50_alpha));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        // Check current app status
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sp.contains(PREF_INIT) || !sp.getBoolean(PREF_INIT, false)) {
            DefaultSettingsApplier.setAllDefaults(this);
            // Launch warm welcome
            displayAppSettings();
        } else {
            displayAppSettings();
        }

    }

    private void displayAppSettings() {
        // Show settings
        setContentView(R.layout.settings);
        updateRenderLocally(true);
        findViewById(R.id.settings_muzei).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open activity
                Intent intent = new Intent(StartupActivity.this, WallpaperSettingsActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.settings_muzei).setOnFocusChangeListener(mFocusListener);

        findViewById(R.id.settings_dashclock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open activity
                Intent intent = new Intent(StartupActivity.this, DashboardSettingsActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.settings_dashclock).setOnFocusChangeListener(mFocusListener);

        findViewById(R.id.settings_screensaver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to system screensaver settings
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            }
        });
        findViewById(R.id.settings_screensaver).setOnFocusChangeListener(mFocusListener);

        findViewById(R.id.settings_credits).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to system credits & legal
                new AlertDialog.Builder(new ContextThemeWrapper(StartupActivity.this, R.style.Theme_AppCompat_Dialog))
                        .setTitle("About NeoDash")
                        .setMessage("Based on Roman Nurik's Muzei and DashClock apps.")
                        .show();
            }
        });
        findViewById(R.id.settings_credits).setOnFocusChangeListener(mFocusListener);
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

    private void updateRenderLocally(boolean renderLocally) {
        final ViewGroup localRenderContainer = (ViewGroup)
                findViewById(net.nurik.roman.muzei.R.id.local_render_container);

        FragmentManager fm = getSupportFragmentManager();
        Fragment localRenderFragment = fm.findFragmentById(net.nurik.roman.muzei.R.id.local_render_container);
        if (renderLocally) {
            if (localRenderFragment == null) {
                fm.beginTransaction()
                        .add(net.nurik.roman.muzei.R.id.local_render_container,
                                MuzeiRendererFragment.createInstance(false, false))
                        .commit();
            }
            if (localRenderContainer.getAlpha() == 1) {
                localRenderContainer.setAlpha(0);
            }
            localRenderContainer.setVisibility(View.VISIBLE);
            localRenderContainer.animate()
                    .alpha(1)
                    .setDuration(1000)
                    .withEndAction(null);
        } else {
            if (localRenderFragment != null) {
                fm.beginTransaction()
                        .remove(localRenderFragment)
                        .commit();
            }
            localRenderContainer.animate()
                    .alpha(0)
                    .setDuration(1000)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            localRenderContainer.setVisibility(View.GONE);
                        }
                    });
        }
    }
}
