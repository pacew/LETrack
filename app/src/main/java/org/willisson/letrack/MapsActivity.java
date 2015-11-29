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

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
	SharedPreferences prefs;
	SharedPreferences.Editor prefs_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
		prefs = getSharedPreferences ("locations", MODE_PRIVATE);
		prefs_editor = prefs.edit ();
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
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom (node.loc, 7));
			rectOptions = new PolylineOptions();
		} else {
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom (bostonish, 7));
			rectOptions = null;
			return;
		}

		for (int idx = 0; idx < hist.size (); idx++) {
			node = hist.get (idx);
			mMap.addMarker(new MarkerOptions().position(node.loc).title(node.tag));
			if (rectOptions != null) {
				rectOptions = rectOptions.add (node.loc);
			}
		}

		Polyline polyline = mMap.addPolyline(rectOptions);
    }

	public ArrayList<LocLog> get_loc_history () {
		int hr, min, idx, absmin, last_pt;
		String time, latkey, lonkey;
        float lat, lon;
		ArrayList<LocLog> hist;
		LocLog node;

		hist = new ArrayList<LocLog> ();

		last_pt = -15;

		for (absmin = 0; absmin < 60 * 24; absmin++) {
			hr = (int) Math.floor (absmin / 60);
			min = absmin % 60;

			time = hr + ":" + String.format ("%02d", min);

			latkey = time + "lat";
			lonkey = time + "lon";

			String[] log = prefs.getString(time, "0 0").split (" ");

			lat = Float.parseFloat (log[0]);
			lon = Float.parseFloat (log[1]);

			if (lat != 0 && lon != 0) {
				if (absmin - last_pt >= 15) {
					last_pt = absmin;
					hist.add (new LocLog (new LatLng (lat, lon), time));
				}
			}
		}

		for (idx = 0; idx < hist.size (); idx++) {
			node = hist.get (idx);
		}

		return (hist);
	}
}
