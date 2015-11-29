package org.willisson.letrack;

import com.google.android.gms.maps.model.LatLng;

public class LocLog {
	public LatLng loc;
	public String tag;
	public boolean marker;

	public LocLog (LatLng latlng, String title, boolean mark) {
		loc = latlng;
		tag = title;
		marker = mark;
	}
}
