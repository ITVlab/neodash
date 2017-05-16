package news.androidtv.neodash.views;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import com.google.android.apps.muzei.render.GLTextureView;
import com.google.android.apps.muzei.render.MuzeiBlurRenderer;
import com.google.android.apps.muzei.render.RealRenderController;
import com.google.android.apps.muzei.render.RenderController;

/**
 * Created by Nick on 5/15/2017.
 */

public class MuzeiView extends GLTextureView implements
        RenderController.Callbacks,
        MuzeiBlurRenderer.Callbacks {
    private MuzeiBlurRenderer mRenderer;
    private RenderController mRenderController;
    private static final int REBLUR_LENGTH = 1000 * 3;

    public MuzeiView(Context context) {
        super(context);
        init();
    }

    public MuzeiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mRenderer = new MuzeiBlurRenderer(getContext(), MuzeiView.this);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mRenderController = new RealRenderController(getContext(), mRenderer,
                MuzeiView.this);
        mRenderer.setDemoMode(false);
        mRenderController.setVisible(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRenderer.hintViewportSize(w, h);
        mRenderController.reloadCurrentArtwork(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        mRenderController.destroy();
        queueEventOnGlThread(new Runnable() {
            @Override
            public void run() {
                mRenderer.destroy();
            }
        });
        super.onDetachedFromWindow();
    }

    @Override
    public void queueEventOnGlThread(Runnable runnable) {
        queueEvent(runnable);
    }

    public void toggleBlurred(final boolean a) {
        mRenderer.setIsBlurred(!mRenderer.isBlurred(), a);
        // Schedule a re-blur
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mRenderer.setIsBlurred(true, false);
            }
        }, REBLUR_LENGTH);
    }
}