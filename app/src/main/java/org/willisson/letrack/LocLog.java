package org.willisson.letrack;

import com.google.android.gms.maps.model.LatLng;

public class LocLog {
	public LatLng loc;
	public String tag;

	public LocLog (LatLng latlng, String title) {
		loc = latlng;
		tag = title;
	}
}
