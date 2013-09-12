package org.foxteam.noisyfox.patronsline;

import com.baidu.mapapi.map.LocationData;

public interface MyLocationListener {

	public void onLocationChanged(LocationData locationData);

	public void onCityChanged(String city);

}
