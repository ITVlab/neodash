package news.androidtv.neodash;

import java.io.FileNotFoundException;
import java.util.Random;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.service.dreams.DreamService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewPropertyAnimator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.apps.muzei.api.MuzeiContract;
import com.google.android.apps.muzei.render.ImageUtil;

/**
 * This class is a sample implementation of a DreamService. When activated, a
 * TextView will repeatedly, move from the left to the right of screen, at a
 * random y-value.
 * <p/>
 * Daydreams are only available on devices running API v17+.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class NeodashDreamService extends DreamService {
    private static final String TAG = NeodashDreamService.class.getSimpleName();

    private boolean isDreaming = false;

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
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
            // Do something(?)
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

        // TODO: Dismantle resources
        // (for example, detach from handlers and listeners).
    }

    private void showWallpaper() {
        if (!isDreaming) {
            return; // Exit.
        }
        ImageView wallpaper = ((ImageView) findViewById(R.id.wallpaper));
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
        wallpaper.setImageBitmap(background);
    }
}
