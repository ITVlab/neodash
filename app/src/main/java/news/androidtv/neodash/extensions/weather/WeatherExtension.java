package news.androidtv.neodash.extensions.weather;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import news.androidtv.neodash.Manifest;
import news.androidtv.neodash.R;

/**
 * Created by Nick on 5/20/2017.
 *
 * Obtains weather data from DarkSky.net and shows it in the extension.
 */

public class WeatherExtension extends DashClockExtension implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = WeatherExtension.class.getSimpleName();

    private static final String DARKSKY_KEY = "6bb985b59e0265149fc0e4d7159ff9b2";

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onUpdateData(int reason) {
        // Called on a background thread.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            publishUpdate(new ExtensionData()
                    .visible(true)
                    .status("Location permission required")
                    .icon(net.nurik.roman.dashclock.R.drawable.ic_place_white_24dp)
                    .clickIntent(null));
        } else {
            // Get user location
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            return;
        }
        // Get location
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        // Make weather API request
        String url = "https://api.forecast.io/forecast/" + DARKSKY_KEY + "/" +
                mLastLocation.getLatitude() + "," +
                mLastLocation.getLongitude() + "?exclude=minutely,flags,hourly,daily&lang=en&units=us";
        try {
            String response = downloadUrl(url); // It's okay we're doing it on background thread.
            // Process data
            JSONObject parsedResponse = new JSONObject(response);
            String summary = parsedResponse.getJSONObject("currently").getString("summary");
            String iconName = parsedResponse.getJSONObject("currently").getString("icon");
            double temperature = parsedResponse.getJSONObject("currently").getDouble("temperature");
            // Publish data
            publishUpdate(new ExtensionData()
                    .visible(false)
                    .status(summary + " — " + temperature + "° F") // F only for now.
                    .icon(getAppropriateIcon(iconName))
                    .clickIntent(null));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            publishUpdate(new ExtensionData()
                    .visible(false)
                    .status(e.getMessage())
                    .icon(net.nurik.roman.dashclock.R.drawable.ic_place_white_24dp)
                    .clickIntent(null));
        } finally {
            // Disconnect
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 1000 characters of the retrieved
        // web page content.
        int len = 1000;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            //set back to 15000, 10000
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int i = is.read();
            while (i != -1)
            {
                byteArrayOutputStream.write(i);
                i = is.read();
            }
            Log.d(TAG, "Download finished; parse");
            return byteArrayOutputStream.toString("UTF-8");
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private int getAppropriateIcon(String iconName) {
        if (iconName.equals("clear-day")) {
            return R.drawable.weather_sun;
        } else if (iconName.equals("clear-night")) {
            return R.drawable.weather_night;
        } else if (iconName.equals("rain")) {
            return R.drawable.weather_rain;
        } else if (iconName.equals("snow")) {
            return R.drawable.ic_weather_snow;
        } else if (iconName.equals("sleet")) {
            return R.drawable.weather_sleet;
        } else if (iconName.equals("wind")) {
            return R.drawable.weather_wind;
        } else if (iconName.equals("fog")) {
            return R.drawable.weather_fog;
        } else if (iconName.equals("cloudy")) {
            return R.drawable.weather_cloud;
        } else if (iconName.equals("partly-cloudy-day")) {
            return R.drawable.weather_partly_cloudy;
        } else if (iconName.equals("partly-cloudy-night")) {
            return R.drawable.weather_partly_cloudy;
        }
        return R.drawable.weather_lightning;
    }
}
