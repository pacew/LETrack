package org.willisson.letrack;

import android.content.Intent;
import android.content.IntentSender;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public final String TAG = "LEtrack";
    public static String selected_dt;

    GoogleApiClient mGoogleApiClient;

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

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        }
        Log.i(TAG, "connecting google api client");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    ListView day_listview;
    ArrayList<String> file_list;
    String[] file_list_arr;

    void make_file_list() {
        SimpleDateFormat dt_fmt = new SimpleDateFormat("yyyyMMdd");
        String today_dt = dt_fmt.format(Calendar.getInstance().getTime());
        if (selected_dt == null)
            selected_dt = today_dt;

        int selected_slot = -1;

        String[] files = fileList();
        file_list = new ArrayList<String>();

        int curslot = 0;
        for (int i = 0; i < files.length; i++) {
            String filename = files[i];
            if (filename.length() < 9)
                continue;
            String prefix = filename.substring(0, 9);
            String dt = filename.substring(9);
            Log.i(TAG, "prefix " + prefix + " dt " + dt);
            if (prefix.equals("locations") && dt.length() == 8) {
                Log.i(TAG, "files: " + prefix + " " + dt);
                file_list.add(dt);

                if (selected_dt.equals(dt))
                    selected_slot = curslot;

                curslot++;
            }
        }
        Log.i(TAG, "selected_slot " + selected_slot);

        Collections.sort(file_list);

        file_list_arr = new String[file_list.size()];
        file_list_arr = file_list.toArray(file_list_arr);
        Log.i(TAG, "nfile_list = " + file_list_arr.length);
        for (String s : file_list_arr)
            Log.i(TAG, s);

        day_listview = (ListView) findViewById(R.id.day_listview);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_activated_1, android.R.id.text1, file_list_arr);

        day_listview.setAdapter(adapter);
        if (selected_slot >= 0)
            day_listview.setItemChecked(selected_slot, true);

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

    public void to_map(View view) {
        if (selected_dt == null) {
            Log.i(TAG, "no date selected");
            return;
        }
        Log.i(TAG, "show map for " + selected_dt);
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void send_toast(String text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void start_click(View view) {
        Log.i(TAG, "start_click");
        Intent intent = new Intent(this, DataProcess.class);
        startService(intent);
    }

    public void stop_button_click(View view) {
        Log.i(TAG, "stop click");
        Intent intent = new Intent(this, DataProcess.class);
        stopService(intent);

    }

    public void hi_precision_click(View view) {
        CheckBox cb = (CheckBox) findViewById(R.id.hi_precision_checkbox);
        boolean val = cb.isChecked();
        Log.i(TAG, "hi precision click " + val);

        Intent intent = new Intent(this, DataProcess.class);
        intent.setAction(val ? "hi_precision" : "lo_precision");
        startService(intent);
    }

    public void sync_button_click(View view) {
        Log.i(TAG, "sync files");
        make_file_list();
        new Thread(new Runnable() {
            public void run() {
                do_sync();
            }
        }).start();
    }

    void delete_folder(DriveId folder_id) {
        DriveFolder folder = folder_id.asDriveFolder();
        Status val = folder.delete(mGoogleApiClient).await();
        Log.i(TAG, "delete status " + val);

    }

    void create_letrack_folder() {
        DriveFolder root = Drive.DriveApi.getRootFolder(mGoogleApiClient);
        MetadataChangeSet cs = new MetadataChangeSet.Builder()
                .setTitle("LEtrack").build();
        DriveFolder.DriveFolderResult val = root.createFolder(mGoogleApiClient, cs).await();
        Log.i(TAG, "create LEtrack folder result: " + val);
    }

    DriveId get_letrack_folder() {
        DriveFolder root = Drive.DriveApi.getRootFolder(mGoogleApiClient);
        Log.i(TAG, "root = " + root);
        Query query = new Query.Builder()
                .addFilter(Filters.and(
                        Filters.eq(SearchableField.TITLE, "LEtrack"),
                        Filters.eq(SearchableField.TRASHED, false)))
                .build();

        DriveApi.MetadataBufferResult result
                = root.queryChildren(mGoogleApiClient, query).await();
        if (!result.getStatus().isSuccess()) {
            Log.i(TAG, "error looking up LEtrack folder");
            return (null);
        }
        MetadataBuffer mbuf = result.getMetadataBuffer();
        Log.i(TAG, "result " + mbuf);
        Log.i(TAG, "lookup count " + mbuf.getCount());
        if (mbuf.getCount() == 0) {
            Log.i(TAG, "not found");
            return (null);
        }
        for (int i = 0; i < mbuf.getCount(); i++) {
            Metadata md = mbuf.get(i);
            Log.i(TAG, "file" + i + " " + md.getTitle()
                    + " trashed " + md.isTrashed()
                    + " dt " + md.getModifiedDate() + " id " + md.getDriveId());
        }

        Metadata md = mbuf.get(0);
        Log.i(TAG, "md = " + md);
        Log.i(TAG, "title = " + md.getTitle());
        DriveId letrack_id = md.getDriveId();
        Log.i(TAG, "driveid = " + letrack_id);
        Log.i(TAG, "enc " + letrack_id.encodeToString());

        mbuf.release();

        return (letrack_id);
    }

    void write_file(DriveFolder folder, String filename, byte[] buf) {
        MetadataChangeSet md = new MetadataChangeSet.Builder()
                .setTitle(filename)
                .setMimeType("application/csv").build();

        DriveContents contents = Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .await()
                .getDriveContents();

        try {
            OutputStream outf = contents.getOutputStream();
            outf.write(buf);
            outf.close();
        } catch (Exception e) {
        }

        DriveFolder.DriveFileResult res = folder.createFile(mGoogleApiClient,
                md, contents).await();
        Log.i(TAG, "file create result = " + res);
        Log.i(TAG, "status " + res.getStatus());
    }

    void copy_out_file (String dt, DriveFolder folder) {
        try {
            String inname = "locations" + dt;
            String outname = "LEtrack" + dt;

            FileInputStream inf = openFileInput(inname);

            MetadataChangeSet md = new MetadataChangeSet.Builder()
                    .setTitle(outname)
                    .setMimeType("text/plain").build();

            DriveContents contents = Drive.DriveApi.newDriveContents(mGoogleApiClient)
                    .await()
                    .getDriveContents();
            OutputStream outf = contents.getOutputStream();

            byte[] buf = new byte[10240];
            int n;
            while ((n = inf.read (buf, 0, buf.length)) > 0) {
                outf.write (buf, 0, n);
            }
            inf.close ();
            outf.close();

            DriveFolder.DriveFileResult res = folder.createFile(mGoogleApiClient,
                    md, contents).await();
            Log.i(TAG, "file create result = " + res);
            Log.i(TAG, "status " + res.getStatus());

        } catch (Exception e) {
            Log.i (TAG, "file write error");
        }
    }

    void do_sync () {
        Log.i(TAG, "doing sync");
        DriveId letrack_folder_id = get_letrack_folder();
        if (letrack_folder_id == null) {
            Log.i (TAG, "try creating LEtrack folder");
            create_letrack_folder ();
            if ((letrack_folder_id = get_letrack_folder()) == null) {
                Log.i(TAG, "can't make LEtrack folder");
                return;
            }
        }
        Log.i(TAG, "letrack folder = " + letrack_folder_id);

        DriveFolder letrack_folder = letrack_folder_id.asDriveFolder();
        Log.i(TAG, "folder " + letrack_folder);
        Log.i(TAG, "file_list_arr = " + file_list_arr);
        for (int i = 0; i < file_list_arr.length; i++) {
            Log.i (TAG, "sync file " + file_list_arr[i]);
            copy_out_file (file_list_arr[i], letrack_folder);
        }
/*
        try {
            write_file(letrack_folder, "hello2.csv", "abc".getBytes("UTF-8"));
        } catch (Exception e) {
            Log.i (TAG, "error writing hello2.csv");
        }
*/

    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "google api connected");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "google api connection suspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            Log.i(TAG, "noresolution path");
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            Log.i(TAG, "calling startresolution 123");
            result.startResolutionForResult(this, 123 );
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i (TAG, "onActivityResult " + resultCode + " " + data);
    }
}
