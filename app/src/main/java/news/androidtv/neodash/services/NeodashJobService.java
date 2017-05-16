package news.androidtv.neodash.services;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.google.android.apps.muzei.sync.DownloadArtworkJobService;

import news.androidtv.neodash.utils.RecommendationBuilder;

/**
 * Created by Nick on 5/15/2017.
 */

public class NeodashJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        RecommendationBuilder.maybeShowNewArtworkNotification(this); // Add notification
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
