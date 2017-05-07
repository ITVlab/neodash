package news.androidtv.neodash.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.apps.muzei.api.MuzeiContract;
import com.google.android.apps.muzei.render.ImageUtil;
import com.google.android.apps.muzei.sync.DownloadArtworkJobService;

import java.io.FileNotFoundException;

import news.androidtv.neodash.Manifest;
import news.androidtv.neodash.utils.RecommendationBuilder;

/**
 * Created by Nick on 5/6/2017.
 */

public class DemoActivity extends Activity {
    private static final String TAG = DemoActivity.class.getSimpleName();

    private ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(new JobInfo.Builder(1,
                    new ComponentName(this, DownloadArtworkJobService.class))
                    .setRequiredNetworkType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                            ? JobInfo.NETWORK_TYPE_NOT_ROAMING
                            : JobInfo.NETWORK_TYPE_ANY)
                    .build());
        } else {
            Toast.makeText(this, "This app won't work on your OS version.", Toast.LENGTH_SHORT).show();
        }

        mImageView = new ImageView(this);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(mImageView);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            obtainArtwork();
            RecommendationBuilder.maybeShowNewArtworkNotification(this); // Add notification
        } else {
            obtainArtwork();
            RecommendationBuilder.maybeShowNewArtworkNotification(this); // Add notification
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        obtainArtwork();
        RecommendationBuilder.maybeShowNewArtworkNotification(this); // Add notification
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void obtainArtwork() {
        ContentResolver contentResolver = getContentResolver();
        Cursor artwork = contentResolver.query(
                MuzeiContract.Artwork.CONTENT_URI,
                new String[] {BaseColumns._ID,
                        MuzeiContract.Artwork.COLUMN_NAME_IMAGE_URI,
                        MuzeiContract.Artwork.COLUMN_NAME_TOKEN,
                        MuzeiContract.Artwork.COLUMN_NAME_TITLE,
                        MuzeiContract.Artwork.COLUMN_NAME_BYLINE,
                        MuzeiContract.Artwork.COLUMN_NAME_VIEW_INTENT,
                        MuzeiContract.Sources.COLUMN_NAME_SUPPORTS_NEXT_ARTWORK_COMMAND,
                        MuzeiContract.Sources.COLUMN_NAME_COMMANDS},
                null, null, null);
        if (artwork == null || !artwork.moveToFirst()) {
            Log.d(TAG, "Artwork: " + artwork);
            if (artwork != null) {
                Log.d(TAG, "Found " + artwork.getCount() + " results");
                artwork.close();
            }
            return;
        }

        long currentArtworkId = artwork.getLong(artwork.getColumnIndex(BaseColumns._ID));
        String currentImageUri = artwork.getString(artwork.getColumnIndex(MuzeiContract.Artwork.COLUMN_NAME_IMAGE_URI));
        String currentToken = artwork.getString(artwork.getColumnIndex(MuzeiContract.Artwork.COLUMN_NAME_TOKEN));
        Log.d(TAG, "Standard content uri is " + MuzeiContract.Artwork.CONTENT_URI);

        Bitmap largeIcon;
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
            largeIcon = BitmapFactory.decodeStream(contentResolver.openInputStream(
                    MuzeiContract.Artwork.CONTENT_URI), null, options);

            // Use the suggested 1920x1080 for Android TV background images
            options.inSampleSize = ImageUtil.calculateSampleSize(height, 1080);
            background = BitmapFactory.decodeStream(contentResolver.openInputStream(
                    MuzeiContract.Artwork.CONTENT_URI), null, options);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to read artwork to show notification", e);
            return;
        }


        Log.d(TAG, currentArtworkId + ", " + currentImageUri + "," + currentToken);
        mImageView.setImageBitmap(background);
    }
}
