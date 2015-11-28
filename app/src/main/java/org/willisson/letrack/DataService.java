package org.willisson.letrack;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by pace on 11/28/15.
 */
public class DataService extends IntentService
        implements LocationListener, GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener {
    public final String TAG = "LEtrack";
    static boolean running;

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

        loc_test();

        while (true) {
            get_datapoint();
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

        LocationRequest req = new LocationRequest();
        req.setInterval (10 * 1000);
        req.setFastestInterval (10 * 1000);
        req.setPriority(LocationRequest.PRIORITY_LOW_POWER);


        LocationServices.FusedLocationApi.requestLocationUpdates (mGoogleApiClient, req, this);

    }

    @Override
    public void onConnectionSuspended (int i) {
        Log.i (TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i (TAG, "location changed " + location);
    }

    void get_datapoint() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        Log.i (TAG, "timestamp " + ts);
    }
}
