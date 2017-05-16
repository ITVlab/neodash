package news.androidtv.neodash.services;

import java.io.FileNotFoundException;
import java.util.List;

import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.service.dreams.DreamService;
import android.support.v4.app.FragmentManager;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.apps.dashclock.DashClockService;
import com.google.android.apps.dashclock.DaydreamService;
import com.google.android.apps.dashclock.ExtensionManager;
import com.google.android.apps.dashclock.PeriodicExtensionRefreshReceiver;
import com.google.android.apps.dashclock.WidgetClickProxyActivity;
import com.google.android.apps.dashclock.render.DashClockRenderer;
import com.google.android.apps.dashclock.render.SimpleRenderer;
import com.google.android.apps.dashclock.render.SimpleViewBuilder;
import com.google.android.apps.muzei.api.MuzeiContract;
import com.google.android.apps.muzei.render.ImageUtil;
import com.google.android.apps.muzei.render.MuzeiRendererFragment;

import news.androidtv.neodash.R;
import news.androidtv.neodash.views.MuzeiView;

import static android.support.v7.graphics.Palette.from;
import static com.google.android.apps.dashclock.Utils.SECONDS_MILLIS;

/**
 * This class is a sample implementation of a DreamService. When activated, a
 * TextView will repeatedly, move from the left to the right of screen, at a
 * random y-value.
 * <p/>
 * Daydreams are only available on devices running API v17+.
 */
/* Extends DashClock's dreamservice */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class NeodashDreamService extends DreamService implements
        ExtensionManager.OnChangeListener {
    private static final String TAG = NeodashDreamService.class.getSimpleName();

    private ExtensionManager mExtensionManager;
    private Handler mHandler = new Handler();
    private ViewGroup mDaydreamContainer;
    private ViewGroup mExtensionsContainer;

    private boolean isDreaming = false;
    private boolean mAttached;
    private boolean mNeedsRelayout;
    private boolean mManuallyAwoken;

    private static final int ANIMATION_HAS_SLIDE = 0x2;
    private static final int ANIMATION_HAS_FADE = 0x4;

    private static final int ANIMATION_FADE = ANIMATION_HAS_FADE;


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Exit dream upon user touch?
        setInteractive(true);

        // Hide system UI?
        setFullscreen(true);

        // Keep screen at full brightness?
        setScreenBright(true);

        // Set the content view, just like you would with an Activity.
        setContentView(R.layout.neodash_dream);
        Log.d(TAG, "Attaching to window");

        mExtensionManager = ExtensionManager.getInstance(this);
        mExtensionManager.addOnChangeListener(this);

        // Update extensions and ensure the periodic refresh is set up.
        PeriodicExtensionRefreshReceiver.updateExtensionsAndEnsurePeriodicRefresh(this);

        mAttached = true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER ||
                event.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
                event.getKeyCode() == KeyEvent.KEYCODE_SPACE ||
                event.getKeyCode() == KeyEvent.KEYCODE_A ||
                event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
            // Do something.
            ((MuzeiView) findViewById(R.id.fancy_wallpaper)).toggleBlurred(false);
            return true; // Consume event.
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        Log.d(TAG, "Start dreaming");
        isDreaming = true;

        // TODO: Begin animations or other behaviors here.
        // TODO Show attributes
        final Handler handler = new Handler(Looper.getMainLooper());

        Runnable displayWallpaper = new Runnable() {
            @Override
            public void run() {
                showWallpaper();
                showDashclock();
                if (isDreaming) {
                    handler.postDelayed(this, 1000 * 60 * 15); // 15 min
                }
            }
        };
        handler.post(displayWallpaper);
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();
        isDreaming = false;
        Log.d(TAG, "Wake up");

        // TODO: Stop anything that was started in onDreamingStarted()
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mExtensionManager.removeOnChangeListener(this);
        mExtensionManager = null;
        mHandler.removeCallbacksAndMessages(null);
        mAttached = false;
    }

    private void showWallpaper() {
        if (!isDreaming) {
            return; // Exit.
        }
        ContentResolver contentResolver = getContentResolver();

        Bitmap background;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(contentResolver.openInputStream(
                    MuzeiContract.Artwork.CONTENT_URI), null, options);
            int width = options.outWidth;
            int height = options.outHeight;
            int shortestLength = Math.min(width, height);
            options.inJustDecodeBounds = false;
            int largeIconHeight = getResources()
                    .getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
            options.inSampleSize = ImageUtil.calculateSampleSize(shortestLength, largeIconHeight);

            // Use the suggested 1920x1080 for Android TV background images
            options.inSampleSize = ImageUtil.calculateSampleSize(height, 1080);
            background = BitmapFactory.decodeStream(contentResolver.openInputStream(
                    MuzeiContract.Artwork.CONTENT_URI), null, options);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to read artwork to show notification", e);
            return;
        }

        Log.d(TAG, "Show artwork " + MuzeiContract.Artwork.CONTENT_URI);
//        wallpaper.setVisibility(View.INVISIBLE);
        showArtworkInfo(background);
    }

    private void showArtworkInfo(Bitmap artBitmap) {
        if (!isDreaming) {
            Log.d(TAG, "We are not dreaming");
            return; // Exit.
        }
        // Show artwork metadata on left.
        ContentResolver contentResolver = getContentResolver();
        Cursor artwork = contentResolver.query(
                MuzeiContract.Artwork.CONTENT_URI,
                new String[] {BaseColumns._ID,
                        MuzeiContract.Artwork.COLUMN_NAME_TITLE,
                        MuzeiContract.Artwork.COLUMN_NAME_BYLINE,
                        MuzeiContract.Artwork.COLUMN_NAME_ATTRIBUTION,
                        MuzeiContract.Sources.COLUMN_NAME_COMPONENT_NAME,
                        MuzeiContract.Sources.COLUMN_NAME_DESCRIPTION},
                null, null, null);
        if (artwork == null || !artwork.moveToFirst()) {
            if (artwork != null) {
                artwork.close();
            }
            return;
        }

        String title = artwork.getString(artwork.getColumnIndex(MuzeiContract.Artwork.COLUMN_NAME_TITLE));
        String artist = artwork.getString(artwork.getColumnIndex(MuzeiContract.Artwork.COLUMN_NAME_BYLINE));
        String attribution = artwork.getString(artwork.getColumnIndex(MuzeiContract.Artwork.COLUMN_NAME_ATTRIBUTION));
        String source = artwork.getString(artwork.getColumnIndex(MuzeiContract.Sources.COLUMN_NAME_DESCRIPTION));
        String component = artwork.getString(artwork.getColumnIndex(MuzeiContract.Sources.COLUMN_NAME_COMPONENT_NAME));
        Log.d(TAG, "Source: " + source);
        Log.d(TAG, "Source2: " + artwork.getString(artwork.getColumnIndex(MuzeiContract.Sources.COLUMN_NAME_COMPONENT_NAME)));
        // Pull source from package
        ComponentName componentName = ComponentName.unflattenFromString(component);
        Intent artworkIntent = new Intent();
        artworkIntent.setComponent(componentName);
        List<ResolveInfo> artworkSources = getPackageManager().queryIntentServices(artworkIntent, PackageManager.MATCH_ALL);
        Log.d(TAG, "Found " + artworkSources.size());

        ((TextView) findViewById(R.id.artwork_name)).setText(title);
        ((TextView) findViewById(R.id.artwork_artist)).setText(artist + "\n" + attribution);
        if (artworkSources.size() >= 1) {
            ((TextView) findViewById(R.id.artwork_source)).setText(artworkSources.get(0).loadLabel(getPackageManager()));
        }
        // Show time on right.

        // Get the proper colors.
        Log.d(TAG, "Artwork bitmap: " + artBitmap.getWidth() + "x" + artBitmap.getHeight());
        Palette palette;
        try {
            palette = Palette.from(artBitmap).setRegion(32, 920, 1920 - 32, 1080).generate();
        } catch (IllegalArgumentException e) {
            // The given region must intersect with the Bitmap's dimensions.
            // Generate on whole thing.
            palette = Palette.from(artBitmap).generate();
        }
        Palette.Swatch colors = palette.getSwatches().get(0);

        ((TextView) findViewById(R.id.artwork_name)).setTextColor(colors.getBodyTextColor());
        ((TextView) findViewById(R.id.artwork_artist)).setTextColor(colors.getTitleTextColor());
        ((TextView) findViewById(R.id.artwork_source)).setTextColor(colors.getTitleTextColor());

        renderDashclock(false, colors.getBodyTextColor());
    }

    private void showDashclock() {
        Log.d(TAG, "Draw Dashclock");
    }

    private int mTextColor = Color.WHITE;
    private void renderDashclock(final boolean restartAnimation) {
        renderDashclock(restartAnimation, mTextColor);
    }

    private void renderDashclock(final boolean restartAnimation, int textColor) {
        if (!mAttached || mExtensionManager == null) {
            return;
        }
        mTextColor = textColor;
        final Resources res = getResources();

        mDaydreamContainer = (ViewGroup) findViewById(R.id.daydream_container);
        DaydreamService.RootLayout rootContainer = (DaydreamService.RootLayout)
                findViewById(R.id.daydream_root);

        rootContainer.setRootLayoutListener(new DaydreamService.RootLayout.RootLayoutListener() {
            @Override
            public void onAwake() {
                Log.d(TAG, "Dream awaken");
            }

            @Override
            public boolean isAwake() {
                return mManuallyAwoken;
            }

            @Override
            public void onSizeChanged(int width, int height) {
                Log.d(TAG, "Size changed to " + width + "x" + height);
            }
        });

        // Animate container into view
        mManuallyAwoken = true;
        setFullscreen(false);
        mDaydreamContainer.animate()
                .alpha(1f)
                .rotation(0)
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(res.getInteger(android.R.integer.config_shortAnimTime));

        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        Log.d(TAG, "Screen info " + displayMetrics.widthPixels + "x" + displayMetrics.heightPixels +
                " at " + displayMetrics.density + "dpi");

        int screenWidthDp = (int) (displayMetrics.widthPixels * 1f / displayMetrics.density);
        int screenHeightDp = (int) (displayMetrics.heightPixels * 1f / displayMetrics.density);

        // Set up rendering
        SimpleRenderer renderer = new SimpleRenderer(this);
        DashClockRenderer.Options options = new DashClockRenderer.Options();
        options.target = DashClockRenderer.Options.TARGET_DAYDREAM;
        options.foregroundColor = textColor;
        options.minWidthDp = screenWidthDp;
        options.minHeightDp = screenHeightDp;
        Log.d(TAG, "Options: " + options.minWidthDp + "x" + options.minHeightDp);
        options.newTaskOnClick = true;
        options.onClickListener = null;
        options.clickIntentTemplate = WidgetClickProxyActivity.getTemplate(this);
        renderer.setOptions(options);

        // Render the clock face
        SimpleViewBuilder vb = renderer.createSimpleViewBuilder();
        vb.useRoot(mDaydreamContainer);
        renderer.renderClockFace(vb, textColor);
        vb.setLinearLayoutGravity(R.id.clock_target, Gravity.CENTER_HORIZONTAL);
        findViewById(R.id.clock_target).setVisibility(View.VISIBLE);
        Log.d(TAG, "Draw clock");

        // Render extensions
        mExtensionsContainer = (ViewGroup) findViewById(R.id.extensions_container);
        mExtensionsContainer.removeAllViews();
        List<ExtensionManager.ExtensionWithData> visibleExtensions
                = mExtensionManager.getVisibleExtensionsWithData();
        Log.d(TAG, "Handle " + visibleExtensions.size() + " extensions");
        for (ExtensionManager.ExtensionWithData ewd : visibleExtensions) {
            Log.d(TAG, "Inflate extension " + ewd.listing.title());
            mExtensionsContainer.addView(
                    (View) renderer.renderExpandedExtension(mExtensionsContainer, null, false,
                            ewd));
        }

        if (mDaydreamContainer.getHeight() == 0 || mNeedsRelayout) {
            Log.d(TAG, "Something is changing");
            ViewTreeObserver vto = mDaydreamContainer.getViewTreeObserver();
            if (vto.isAlive()) {
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver vto = mDaydreamContainer.getViewTreeObserver();
                        if (vto.isAlive()) {
                            vto.removeOnGlobalLayoutListener(this);
                        }
                        Log.d(TAG, "restart animation...");
                        postLayoutRender(restartAnimation);
                    }
                });
            }
            mDaydreamContainer.requestLayout();
            mNeedsRelayout = false;
        } else {
            Log.d(TAG, "postLayoutRender");
            postLayoutRender(restartAnimation);
        }
    }

    /**
     * Post-layout render code.
     */
    public void postLayoutRender(boolean restartAnimation) {
        // Adjust the ScrollView
        DaydreamService.ExposedScrollView scrollView = (DaydreamService.ExposedScrollView) findViewById(R.id.extensions_scroller);
        int maxScroll = scrollView.computeVerticalScrollRange() - scrollView.getHeight();
        if (maxScroll < 0) {
            ViewGroup.LayoutParams lp = scrollView.getLayoutParams();
            lp.height = mExtensionsContainer.getHeight();
            scrollView.setLayoutParams(lp);
            mDaydreamContainer.requestLayout();
        }

        // Recolor widget
//        Utils.traverseAndRecolor(mDaydreamContainer, mForegroundColor, true, true);

        /* if (restartAnimation) {
            int x = 0;
            int deg = 0;
            if ((mAnimation & ANIMATION_HAS_SLIDE) != 0) {
                x = (mMovingLeft ? 1 : -1) * mTravelDistance;
            }
            if ((mAnimation & ANIMATION_HAS_ROTATE) != 0) {
                deg = (mMovingLeft ? 1 : -1) * TRAVEL_ROTATE_DEGREES;
            }
            mMovingLeft = !mMovingLeft;
            mDaydreamContainer.animate().cancel();
            if ((mAnimation & ANIMATION_HAS_SLIDE) != 0) {
                // Only use small size when moving
                mDaydreamContainer.setScaleX(SCALE_WHEN_MOVING);
                mDaydreamContainer.setScaleY(SCALE_WHEN_MOVING);
            }
            if (mSingleCycleAnimator != null) {
                mSingleCycleAnimator.cancel();
            }

            Animator scrollDownAnimator = ObjectAnimator.ofInt(scrollView,
                    DaydreamService.ExposedScrollView.SCROLL_POS, 0, maxScroll);
            scrollDownAnimator.setDuration(CYCLE_INTERVAL_MILLIS / 5);
            scrollDownAnimator.setStartDelay(CYCLE_INTERVAL_MILLIS / 5);

            Animator scrollUpAnimator = ObjectAnimator.ofInt(scrollView,
                    DaydreamService.ExposedScrollView.SCROLL_POS, 0);
            scrollUpAnimator.setDuration(CYCLE_INTERVAL_MILLIS / 5);
            scrollUpAnimator.setStartDelay(CYCLE_INTERVAL_MILLIS / 5);

            AnimatorSet scrollAnimator = new AnimatorSet();
            scrollAnimator.playSequentially(scrollDownAnimator, scrollUpAnimator);

            Animator moveAnimator = ObjectAnimator.ofFloat(mDaydreamContainer,
                    View.TRANSLATION_X, x, -x).setDuration(CYCLE_INTERVAL_MILLIS);
            moveAnimator.setInterpolator(new LinearInterpolator());

            Animator rotateAnimator = ObjectAnimator.ofFloat(mDaydreamContainer,
                    View.ROTATION, deg, -deg).setDuration(CYCLE_INTERVAL_MILLIS);
            moveAnimator.setInterpolator(new LinearInterpolator());

            mSingleCycleAnimator = new AnimatorSet();
            mSingleCycleAnimator.playTogether(scrollAnimator, moveAnimator, rotateAnimator);
            mSingleCycleAnimator.start();
        } */
    }

    private Runnable mHandleExtensionsChanged = new Runnable() {
        @Override
        public void run() {
            renderDashclock(false);
        }
    };

    @Override
    public void onExtensionsChanged(ComponentName sourceExtension) {
        mHandler.removeCallbacks(mHandleExtensionsChanged);
        mHandler.postDelayed(mHandleExtensionsChanged,
                DashClockService.UPDATE_COLLAPSE_TIME_MILLIS);
    }

    private void updateRenderLocally(boolean renderLocally) {

    }
}
