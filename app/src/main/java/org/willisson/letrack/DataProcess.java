package org.willisson.letrack;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by pace on 11/29/15.
 */
public class DataProcess extends Service
        implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            LocationListener {
    public static String TAG = "LEtrack";
    static GoogleApiClient gapi;
	static String filename;
	static FileOutputStream outf;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "create data process");

        Log.i(TAG, "setting foreground");

        Intent notificationIntent = new Intent(this, DataService.class);
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 0,
                notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

        Notification notification=new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_trending_neutral_24dp)
                .setContentText("LEtrack running")
                .setContentIntent(pendingIntent).build();

        startForeground(31926, notification);

        gapi = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        gapi.connect();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"dataprocess destroy");
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "onconnected");
        try {
            LocationRequest req = new LocationRequest();
            req.setInterval(60 * 1000);
            req.setFastestInterval(60 * 1000);
            req.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(gapi, req, this);
        } catch (Exception e) {
            Log.i (TAG, "error starting location requests");
            Toast toast = Toast.makeText (this,
                    "can't get location - check permissions",
                    Toast.LENGTH_LONG);
            toast.show();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
			Calendar now = Calendar.getInstance();
			Date t = now.getTime();
			SimpleDateFormat dt_fmt = new SimpleDateFormat ("yyyyMMdd");
			SimpleDateFormat time_fmt = new SimpleDateFormat ("HHmm");
			String dt = dt_fmt.format(t);
			String hhmm = time_fmt.format (t);

			String val = hhmm + " "
				+ location.getLatitude() + " "
				+ location.getLongitude();
			Log.i(TAG, "location changed " + val);

			String new_filename = "locations" + dt;

			if (! new_filename.equals (filename)) {
				filename = new_filename;
				if (outf != null)
					outf.close ();
				outf = openFileOutput (filename, MODE_PRIVATE | MODE_APPEND);
			}

            outf.write(val.getBytes("UTF-8"));
            outf.write("\n".getBytes("UTF-8"));
            outf.flush();
        } catch (Exception e) {
            Log.i (TAG, "write error");
        }


    }
}
