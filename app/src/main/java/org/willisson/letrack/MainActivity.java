package org.willisson.letrack;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import android.content.Context;
import android.view.View;
import android.widget.Toast;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public final String TAG = "LEtrack";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext()) != ConnectionResult.SUCCESS) {
			send_toast("Google Play Services not found.");
			finish();
		}
    }

	public void to_map (View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
	}

    public void send_toast (String text) {
		Context context = getApplicationContext ();
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText (context, text, duration);
		toast.show();
    }

    public void start_click (View view) {
        Log.i(TAG, "start_click");
        if (false) {
            Intent intent = new Intent(this, DataService.class);
            intent.setAction("start");
            startService(intent);
        } else {
            Intent intent = new Intent (this, DataProcess.class);
            startService (intent);
        }
    }

    public void stop_button_click (View view) {
        Log.i(TAG, "stop click");
        if (false) {
            DataService.keep_going = false;
            Toast toast = Toast.makeText(this, "Location tracking shutting down",
                    Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Intent intent = new Intent (this, DataProcess.class);
            stopService(intent);
        }
    }
}
