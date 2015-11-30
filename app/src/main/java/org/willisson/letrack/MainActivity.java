package org.willisson.letrack;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ListView;
import android.util.Log;

import org.w3c.dom.DOMStringList;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public final String TAG = "LEtrack";
    public static String selected_dt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "main activity create");
        setContentView(R.layout.activity_main);
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext()) != ConnectionResult.SUCCESS) {
            send_toast("Google Play Services not found.");
            finish();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "mainactivity start");

        make_file_list();
    }

    ListView day_listview;

    void make_file_list() {
        ArrayList<String> choices = new ArrayList<String>();

        String[] files = fileList();
        for (int i = 0; i < files.length; i++) {
            String filename = files[i];
            if (filename.length() < 9)
                continue;
            String prefix = filename.substring(0, 9);
            String dt = filename.substring(9);
            Log.i(TAG, "prefix " + prefix + " dt " + dt);
            if (prefix.equals("locations") && dt.length() == 8) {
                Log.i(TAG, "files: " + prefix + " " + dt);
                choices.add(dt);
            }
        }
        String[] choices_arr = new String[choices.size()];
        choices_arr = choices.toArray(choices_arr);
        Log.i(TAG, "nchoices = " + choices_arr.length);
        for (String s : choices_arr)
            Log.i(TAG, s);

        day_listview = (ListView) findViewById(R.id.day_listview);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, choices_arr);

        day_listview.setAdapter(adapter);

        day_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String val = (String) day_listview.getItemAtPosition(position);
                Log.i(TAG, "click on " + position + " = " + val);

                selected_dt = val;
            }
        });
    }

    public void to_map (View view) {
        if (selected_dt == null) {
            Log.i (TAG, "no date selected");
            return;
        }
        Log.i (TAG, "show map for " + selected_dt);
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
