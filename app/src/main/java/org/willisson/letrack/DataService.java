package org.willisson.letrack;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by pace on 11/28/15.
 */
public class DataService extends IntentService
        implements LocationListener, GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener {
    public final String TAG = "LEtrack";
    static boolean running;

    SharedPreferences prefs;
    SharedPreferences.Editor prefs_editor;


    public DataService() {
        super("DataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (running) {
            Log.i(TAG, "strange: trying to restart data service");
            return;
        }
        Log.i(TAG, "starting data service");
        running = true;

        prefs = getSharedPreferences("locations", MODE_PRIVATE);
        prefs_editor = prefs.edit();

        loc_test();

        while (true) {
            int interval = 10; /* seconds */

            try {
                Thread.sleep(interval * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    GoogleApiClient mGoogleApiClient;

    void loc_test () {


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected (Bundle bundle) {
        Log.i(TAG, "onconnected");
        try {
            LocationRequest req = new LocationRequest();
            req.setInterval(60 * 1000);
            req.setFastestInterval(60 * 1000);
            req.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, req, this);
        } catch (Exception e) {
            Log.i (TAG, "error starting location requests");
            Context context = getApplicationContext ();
            Toast toast = Toast.makeText (context,
                    "can't get location - check permissions",
                    Toast.LENGTH_LONG);
            toast.show();
        }

    }

    @Override
    public void onConnectionSuspended (int i) {
        Log.i(TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat ("yyyyMMdd'T'HHmm");
        String ts = fmt.format (now.getTime ());

        String val = "" + location.getLatitude() + " " + location.getLongitude();
        Log.i (TAG, "location changed " + ts + " " + val);

        prefs_editor.putString(ts, val);
        prefs_editor.commit();

    }

    void get_datapoint() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        Log.i (TAG, "timestamp " + ts);
    }
}
