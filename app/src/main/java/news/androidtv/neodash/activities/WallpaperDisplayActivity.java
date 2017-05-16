package news.androidtv.neodash.activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.apps.muzei.render.MuzeiRendererFragment;
import com.google.android.apps.muzei.settings.SettingsAdvancedFragment;
import com.google.android.apps.muzei.util.DrawInsetsFrameLayout;

import news.androidtv.neodash.R;
import news.androidtv.neodash.views.MuzeiView;

/**
 * Created by Nick on 5/15/2017.
 */

public class WallpaperDisplayActivity extends AppCompatActivity {
    private ObjectAnimator mBackgroundAnimator;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        setContentView(R.layout.wallpaper_settings);
        getSupportActionBar().hide();

        findViewById(R.id.button_sources).setVisibility(View.GONE);
        findViewById(R.id.button_display).setVisibility(View.GONE);

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

        if (mBackgroundAnimator != null) {
            mBackgroundAnimator.cancel();
        }

        mBackgroundAnimator = ObjectAnimator.ofFloat(this, "backgroundOpacity", 0, 1);
        mBackgroundAnimator.setDuration(100);
        mBackgroundAnimator.start();

        inflateSources();
        updateRenderLocally(true);
    }

    private void inflateSources() {
        Class<? extends Fragment> fragmentClass = SettingsAdvancedFragment.class;
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(
                R.id.content_container);
        if (currentFragment != null && fragmentClass.equals(currentFragment.getClass())) {
            return;
        }

        try {
            Fragment newFragment = fragmentClass.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .setTransitionStyle(net.nurik.roman.muzei.R.style.Muzei_SimpleFadeFragmentAnimation)
                    .replace(R.id.content_container, newFragment)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateRenderLocally(boolean renderLocally) {
        final View uiContainer = findViewById(R.id.container);
        final ViewGroup localRenderContainer = (ViewGroup)
                findViewById(R.id.local_render_container);

        FragmentManager fm = getSupportFragmentManager();
        Fragment localRenderFragment = fm.findFragmentById(R.id.local_render_container);
        if (renderLocally) {
            if (localRenderFragment == null) {
                fm.beginTransaction()
                        .add(R.id.local_render_container,
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
            uiContainer.setBackgroundColor(0x00000000); // for ripple touch feedback
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
            uiContainer.setBackground(null);
        }
    }
}
