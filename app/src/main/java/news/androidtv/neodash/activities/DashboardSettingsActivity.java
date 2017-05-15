package news.androidtv.neodash.activities;

import android.app.Fragment;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.dashclock.DashClockService;
import com.google.android.apps.dashclock.HelpUtils;
import com.google.android.apps.dashclock.LogUtils;
import com.google.android.apps.dashclock.RecentTasksStyler;
import com.google.android.apps.dashclock.Utils;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.configuration.ConfigurationActivity;
import com.google.android.apps.dashclock.configuration.ConfigureAdvancedFragment;
import com.google.android.apps.dashclock.configuration.ConfigureAppearanceFragment;
import com.google.android.apps.dashclock.configuration.ConfigureDaydreamFragment;
import com.google.android.apps.dashclock.configuration.ConfigureExtensionsFragment;
import com.google.android.apps.muzei.render.MuzeiRendererFragment;
import com.google.android.apps.muzei.util.DrawInsetsFrameLayout;

import net.nurik.roman.dashclock.BuildConfig;

import news.androidtv.neodash.R;

import static com.google.android.apps.dashclock.LogUtils.LOGD;

/**
 * Created by Nick on 5/14/2017.
 *
 * DashClock Configuration Activity
 */

public class DashboardSettingsActivity extends AppCompatActivity {
    private static final String TAG = LogUtils.makeLogTag(com.google.android.apps.dashclock.configuration.ConfigurationActivity.class);

    public static final String LAUNCHER_ACTIVITY_NAME =
            "com.google.android.apps.dashclock.configuration.ConfigurationLauncherActivity";

    public static final String EXTRA_START_SECTION =
            "com.google.android.apps.dashclock.configuration.extra.START_SECTION";

    public static final int START_SECTION_EXTENSIONS = 0;
    public static final int START_SECTION_APPEARANCE = 1;
    public static final int START_SECTION_DAYDREAM = 2;
    public static final int START_SECTION_ADVANCED = 3;

    private static final int[] SECTION_LABELS = {
            net.nurik.roman.dashclock.R.string.section_extensions,
            net.nurik.roman.dashclock.R.string.section_appearance,
            net.nurik.roman.dashclock.R.string.section_daydream,
            net.nurik.roman.dashclock.R.string.section_advanced,
    };

    @SuppressWarnings("unchecked")
    private static final Class<? extends Fragment>[] SECTION_FRAGMENTS = new Class[]{
            news.androidtv.neodash.fragments.ConfigureExtensionsFragment.class,
            ConfigureAppearanceFragment.class,
            ConfigureDaydreamFragment.class,
            ConfigureAdvancedFragment.class,
    };

    // only used when adding a new widget
    private int mNewWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private int mStartSection = START_SECTION_EXTENSIONS;

    private boolean mBackgroundCleared = false;

    private Toolbar mAppBar;

    public void onCreate(Bundle savedInstanceState) {
        setupFauxDialog();
        RecentTasksStyler.styleRecentTasksEntry(this);
        super.onCreate(savedInstanceState);

        Utils.enableDisablePhoneOnlyExtensions(this);

        Intent intent = getIntent();
        if (intent != null) {
            if (Intent.ACTION_CREATE_SHORTCUT.equals(intent.getAction())) {
                Intent.ShortcutIconResource icon = new Intent.ShortcutIconResource();
                icon.packageName = getPackageName();
                icon.resourceName = getResources().getResourceName(net.nurik.roman.dashclock.R.mipmap.ic_launcher);
                setResult(RESULT_OK, new Intent()
                        .putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(net.nurik.roman.dashclock.R.string.title_configure))
                        .putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon)
                        .putExtra(Intent.EXTRA_SHORTCUT_INTENT,
                                Intent.makeMainActivity(
                                        new ComponentName(this, com.google.android.apps.dashclock.configuration.ConfigurationActivity.class))));
                finish();
            }

            mStartSection = intent.getIntExtra(EXTRA_START_SECTION, 0);
        }

        setContentView(R.layout.dashboard_settings);

        if (intent != null
                && AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(intent.getAction())) {
            mNewWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mNewWidgetId);
            // See http://code.google.com/p/android/issues/detail?id=2539
            setResult(RESULT_CANCELED, new Intent()
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mNewWidgetId));
        }

        ((DrawInsetsFrameLayout) findViewById(R.id.draw_insets_frame_layout)).setOnInsetsCallback(
                new DrawInsetsFrameLayout.OnInsetsCallback() {
                    @Override
                    public void onInsetsChanged(Rect insets) {
                        View container = findViewById(R.id.container);
                        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)
                                container.getLayoutParams();
                        lp.leftMargin = insets.left;
                        lp.topMargin = insets.top;
                        lp.rightMargin = insets.right;
                        lp.bottomMargin = insets.bottom;
                        container.setLayoutParams(lp);
                    }
                });

        // Set up UI widgets
//        setupActionBar();
        getSupportActionBar().hide();
        findViewById(R.id.button_extensions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent playStoreIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/search?q=DashClock+Extension"
                                    + "&c=apps"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                    startActivity(playStoreIntent);
                } catch (ActivityNotFoundException activityNotFoundException1) {
                    Toast.makeText(DashboardSettingsActivity.this,
                            net.nurik.roman.muzei.R.string.play_store_not_found, Toast.LENGTH_LONG).show();
                }
            }
        });
        updateRenderLocally(true);

        setupExtensionsEditor();
    }

    private void updateRenderLocally(boolean renderLocally) {
        final ViewGroup localRenderContainer = (ViewGroup)
                findViewById(net.nurik.roman.muzei.R.id.local_render_container);

        FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.Fragment localRenderFragment = fm.findFragmentById(net.nurik.roman.muzei.R.id.local_render_container);
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

    private void setupFauxDialog() {
        // Check if this should be a dialog
        TypedValue tv = new TypedValue();
        if (!getTheme().resolveAttribute(net.nurik.roman.dashclock.R.attr.isDialog, tv, true) || tv.data == 0) {
            return;
        }

        // Should be a dialog; set up the window parameters.
        DisplayMetrics dm = getResources().getDisplayMetrics();

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getResources().getDimensionPixelSize(net.nurik.roman.dashclock.R.dimen.configure_dialog_width);
        params.height = Math.min(
                getResources().getDimensionPixelSize(net.nurik.roman.dashclock.R.dimen.configure_dialog_max_height),
                dm.heightPixels * 3 / 4);
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        showWallpaper();
    }

    public void showWallpaper() {
        if (!mBackgroundCleared) {
            // We initially show a background so that the activity transition (zoom-up animation
            // in Android 4.1) doesn't show the system wallpaper (which is needed in the
            // appearance configuration fragment). Upon user interaction (i.e. once we know the
            // activity transition has finished), clear the background so that the system wallpaper
            // can be seen when the appearance configuration fragment is shown.
            findViewById(net.nurik.roman.dashclock.R.id.content_container).setBackground(null);
            mBackgroundCleared = true;
        }
    }

    private void setupActionBar() {
        mAppBar = (Toolbar) findViewById(net.nurik.roman.dashclock.R.id.app_bar);

        // Done button
        mAppBar.setNavigationIcon(net.nurik.roman.dashclock.R.drawable.ic_action_done);
        mAppBar.setNavigationContentDescription(net.nurik.roman.dashclock.R.string.done);
        mAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "Done"
                if (mNewWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    setResult(RESULT_OK, new Intent()
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mNewWidgetId));
                }

                finish();
            }
        });

        // Spinner
        final LayoutInflater inflater = LayoutInflater.from(this);
        Spinner sectionSpinner = (Spinner) findViewById(net.nurik.roman.dashclock.R.id.app_bar_section_spinner);
        sectionSpinner.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return SECTION_LABELS.length;
            }

            @Override
            public Object getItem(int position) {
                return SECTION_LABELS[position];
            }

            @Override
            public long getItemId(int position) {
                return position + 1;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = inflater.inflate(net.nurik.roman.dashclock.R.layout.list_item_configure_ab_spinner,
                            parent, false);
                }
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(
                        getString(SECTION_LABELS[position]));
                return convertView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = inflater.inflate(net.nurik.roman.dashclock.R.layout.list_item_configure_ab_spinner_dropdown,
                            parent, false);
                }
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(
                        getString(SECTION_LABELS[position]));
                return convertView;
            }
        });

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                Class<? extends Fragment> fragmentClass = SECTION_FRAGMENTS[position];
                Fragment currentFragment = getFragmentManager().findFragmentById(
                        net.nurik.roman.dashclock.R.id.content_container);
                if (currentFragment != null && fragmentClass.equals(currentFragment.getClass())) {
                    return;
                }

                try {
                    Fragment newFragment = fragmentClass.newInstance();
                    getFragmentManager().beginTransaction()
                            .replace(net.nurik.roman.dashclock.R.id.content_container, newFragment)
                            .commitAllowingStateLoss();
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> spinner) {
            }
        });

        sectionSpinner.setSelection(mStartSection);

        // Actions
        mAppBar.inflateMenu(net.nurik.roman.dashclock.R.menu.configure_overflow);
        Menu menu = mAppBar.getMenu();
        if (!BuildConfig.DEBUG) {
            MenuItem sendLogsItem = menu.findItem(net.nurik.roman.dashclock.R.id.action_send_logs);
            if (sendLogsItem != null) {
                sendLogsItem.setVisible(false);
            }
        }

        mAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == net.nurik.roman.dashclock.R.id.action_get_more_extensions) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/search?q=DashClock+Extension"
                                    + "&c=apps"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    return true;
                } else if (item.getItemId() == net.nurik.roman.dashclock.R.id.action_send_logs) {
                    LogUtils.sendDebugLog(DashboardSettingsActivity.this);
                    return true;
                } else if (item.getItemId() == net.nurik.roman.dashclock.R.id.action_about) {
                    HelpUtils.showAboutDialog(DashboardSettingsActivity.this);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Update extensions (settings may have changed)
        // TODO: update only those extensions whose settings have changed
        Intent updateExtensionsIntent = new Intent(this, DashClockService.class);
        updateExtensionsIntent.setAction(DashClockService.ACTION_UPDATE_EXTENSIONS);
        updateExtensionsIntent.putExtra(DashClockService.EXTRA_UPDATE_REASON,
                DashClockExtension.UPDATE_REASON_SETTINGS_CHANGED);
        startService(updateExtensionsIntent);

        // Update all widgets, including a new one if it was just added
        // We can't only update the new one because settings affecting all widgets may have
        // been changed.
        LOGD(TAG, "Updating all widgets");

        Intent widgetUpdateIntent = new Intent(this, DashClockService.class);
        widgetUpdateIntent.setAction(DashClockService.ACTION_UPDATE_WIDGETS);
        startService(widgetUpdateIntent);
    }

    private void setupExtensionsEditor() {
        Class<? extends Fragment> fragmentClass = SECTION_FRAGMENTS[0];
        Fragment currentFragment = getFragmentManager().findFragmentById(
                net.nurik.roman.dashclock.R.id.content_container);
        if (currentFragment != null && fragmentClass.equals(currentFragment.getClass())) {
            return;
        }

        try {
            Fragment newFragment = fragmentClass.newInstance();
            getFragmentManager().beginTransaction()
                    .replace(net.nurik.roman.dashclock.R.id.content_container, newFragment)
                    .commitAllowingStateLoss();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
