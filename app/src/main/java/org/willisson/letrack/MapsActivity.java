package org.willisson.letrack;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    static String TAG = "LEtrack";

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
		LocLog node;
		PolylineOptions rectOptions;
		ArrayList<LocLog> hist;

        mMap = googleMap;

		hist = get_loc_history ();

		LatLng bostonish = new LatLng (42, -71);

		if (hist.size () > 0) {
			node = hist.get (0);
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom (node.loc, 12));
			rectOptions = new PolylineOptions();
		} else {
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom (bostonish, 12));
			rectOptions = null;
			return;
		}

		for (int idx = 0; idx < hist.size (); idx++) {
			node = hist.get(idx);
			if (node.marker || idx == hist.size () - 1) {
				mMap.addMarker(new MarkerOptions().position(node.loc).title(node.tag));
			}
			if (rectOptions != null) {
				rectOptions = rectOptions.add (node.loc);
                Log.i (TAG, "add point " + node.tag + " " + node.loc.latitude + " " + node.loc.longitude);

            }
		}

		Polyline polyline = mMap.addPolyline(rectOptions);
    }

	public ArrayList<LocLog> get_loc_history () {
		int hr, min, idx, absmin, last_pt;
		String hhmm, tag;
        float lat, lon;
		ArrayList<LocLog> hist;
        
		hist = new ArrayList<LocLog> ();

		last_pt = -15;

        try {
			String filename = "locations" + MainActivity.selected_dt;
            Log.i (TAG, "map: open file " + filename);
            FileInputStream fin = openFileInput(filename);
            BufferedReader rd = new BufferedReader(new InputStreamReader(fin));
            String row;

            while ((row = rd.readLine()) != null) {
				String[] elts = row.split (" ");
				String ts = elts[0];
				lat = Float.parseFloat (elts[1]);
				lon = Float.parseFloat(elts[2]);

				hhmm = ts.substring (ts.length() - 4);
				hr = Integer.parseInt (hhmm.substring(0, 2));
				min = Integer.parseInt (hhmm.substring (2, 4));

				absmin = hr * 60 + min;

				Log.i (TAG, "hr "+hr + " min "+min + " lat "+lat + " lon "+lon);

				tag = "";
				if (hr % 12 == 0) {
					tag += "12";
				} else {
					tag += String.format ("%d", hr % 12);
				}
				tag += ":" + String.format ("%02d", min);
				if (hr < 12) {
					tag += " AM";
				} else {
					tag += " PM";
				}

				if (lat != 0 && lon != 0) {
					Boolean add_marker = false;
					if (absmin - last_pt >= 15) {
						last_pt = absmin;
						add_marker = true;
					}
					hist.add (new LocLog(new LatLng(lat,lon),tag,add_marker));
				}
			}
		} catch (Exception e) {
			Log.i (TAG, "parse error");
            e.printStackTrace();
		}
		return (hist);
	}


}
