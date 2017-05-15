package news.androidtv.neodash.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.apps.muzei.render.MuzeiRendererFragment;

import news.androidtv.neodash.R;

/**
 * Created by Nick on 5/14/2017.
 */

public class DemoRenderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_demo_render);
        updateRenderLocally(true);
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
                    .setDuration(2000)
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
