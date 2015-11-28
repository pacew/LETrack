package org.willisson.letrack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import android.content.Context;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext()) != ConnectionResult.SUCCESS) {
			send_toast("Google Play Services not found.");
			finish();
		}
    }

    public void send_toast (String text) {
		Context context = getApplicationContext ();
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText (context, text, duration);
		toast.show();
    }

}
