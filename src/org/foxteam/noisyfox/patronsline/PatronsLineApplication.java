package org.foxteam.noisyfox.patronsline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class PatronsLineApplication extends Application {
	public static final String strKey = "20891f79bb7198708fdba6cf61cf2351";

	public static final String SP_VALUE_INT_LOCATION_LATITUDE = "location_latitude";
	public static final String SP_VALUE_INT_LOCATION_LONGITUDE = "location_longitude";
	public static final String SP_VALUE_STRING_LOCATION_CITY = "location_city";

	public final static String PATH_APP_WORKDIR = Environment
			.getExternalStorageDirectory().getPath() + "/.patronsline";

	static PatronsLineApplication mInstance = null;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;

		mDefualtPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		loadLocationData();
		initEngineManager();
		initLocationClient();

		File f = new File(PATH_APP_WORKDIR);
		f.mkdirs();
	}

	public static PatronsLineApplication getApplication() {
		return mInstance;
	}

	private SharedPreferences mDefualtPreferences = null;
	private BMapManager mBMapManager = null;
	boolean mIsInited = false;
	MKSearchListener mMKSearchListener = new MKSearchListener() {
		@Override
		public void onGetPoiDetailSearchResult(int type, int error) {
		}

		@Override
		public void onGetPoiResult(MKPoiResult res, int type, int error) {
		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult res, int error) {
		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult res, int error) {
		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult res, int error) {
		}

		@Override
		public void onGetAddrResult(MKAddrInfo res, int error) {
			if (res == null || error != 0) {
				return;
			}

			if (mCity != res.addressComponents.city) {
				mCity = res.addressComponents.city;
				for (MyLocationListener ll : mLocListenerList) {
					ll.onCityChanged(mCity);
				}
			}

			Log.d("locChange", mCity);
		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult result, int iError) {
		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult res, int arg1) {
		}

		@Override
		public void onGetShareUrlResult(MKShareUrlResult result, int type,
				int error) {
		}
	};

	// 定位服务
	private LocationClient mLocClient = null;
	private LocationData mLocData = new LocationData();
	private GeoPoint mLastLocation = null;
	private String mCity = "";
	private long mCityUpdate = 0;
	private List<MyLocationListener> mLocListenerList = new ArrayList<MyLocationListener>();
	// 定位SDK监听函数
	private BDLocationListener mLocListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}

			mLocData.latitude = location.getLatitude();
			mLocData.longitude = location.getLongitude();
			// 如果不显示定位精度圈，将accuracy赋值为0即可
			mLocData.accuracy = location.getRadius();
			mLocData.direction = location.getDerect();
			Log.d("locChange", mLocData.latitude + ":" + mLocData.longitude);
			saveLocationData();
			updateCity();

			for (MyLocationListener ll : mLocListenerList) {
				ll.onLocationChanged(mLocData);
			}
		}

		@Override
		public void onReceivePoi(BDLocation poiLocation) {
			// if (poiLocation == null) {
			// return;
			// }
		}

	};

	private void updateCity() {
		long currentTime = System.currentTimeMillis();
		GeoPoint currentLoc = MapUtil.getGeoPoint(mLocData);
		double distance = DistanceUtil.getDistance(currentLoc, mLastLocation);
		Log.d("locChange", "distance:" + distance);
		if (distance > 5000 || currentTime - mCityUpdate > 5 * 60 * 1000) {
			// Log.d("", "UpdateCity");
			MKSearchOperator.reverseGeocode(mMKSearchListener, currentLoc);
			mCityUpdate = currentTime;
			mLastLocation = currentLoc;
			Log.d("locChange", "UpdateCity");
		}
	}

	public void initEngineManager() {
		if (mBMapManager == null) {
			mBMapManager = new BMapManager(this);
			mIsInited = false;
		}

		if (!mIsInited) {
			if (!mBMapManager.init(strKey, new MyGeneralListener())) {
				Toast.makeText(this, "BMapManager 初始化错误!", Toast.LENGTH_LONG)
						.show();
				// mIsInited = false;
			} else {
				MKSearchOperator.init(mBMapManager);
				mIsInited = true;
			}
		}

	}

	public void initLocationClient() {
		if (mLocClient == null) {
			mLocClient = new LocationClient(this);
			mLocClient.registerLocationListener(mLocListener);
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true);// 打开gps
			option.setCoorType("bd09ll"); // 设置坐标类型
			option.setScanSpan(5000);
			mLocClient.setLocOption(option);
			mLocListenerList.clear();
		}
	}

	public void destroyEngineManager() {
		if (mBMapManager != null) {
			mBMapManager.destroy();
			mBMapManager = null;
		}
	}

	public void destroyLocationClient() {
		if (mLocClient != null) {
			mLocClient.stop();
			mLocClient = null;
			mLocListenerList.clear();
		}
	}

	public BMapManager getMapManager() {
		return mBMapManager;
	}

	public LocationClient getLocClient() {
		return mLocClient;
	}

	public LocationData getLocData() {
		return mLocData;
	}

	public String getCity() {
		return mCity;
	}

	public void registerLocationListener(MyLocationListener ll) {
		if (!mLocListenerList.contains(ll)) {
			mLocListenerList.add(ll);
		}
	}

	public void unregisterLocationListener(MyLocationListener ll) {
		mLocListenerList.remove(ll);
	}

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	static class MyGeneralListener implements MKGeneralListener {

		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				Toast.makeText(
						PatronsLineApplication.getApplication()
								.getApplicationContext(), "您的网络出错啦！",
						Toast.LENGTH_LONG).show();
			} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				Toast.makeText(
						PatronsLineApplication.getApplication()
								.getApplicationContext(), "输入正确的检索条件！",
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onGetPermissionState(int iError) {
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
				// 授权Key错误：
				Toast.makeText(
						PatronsLineApplication.getApplication()
								.getApplicationContext(),
						"请在 DemoApplication.java文件输入正确的授权Key！",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void saveLocationData() {
		Editor e = mDefualtPreferences.edit();
		e.putInt(SP_VALUE_INT_LOCATION_LATITUDE,
				(int) (mLocData.latitude * 1E6));
		e.putInt(SP_VALUE_INT_LOCATION_LONGITUDE,
				(int) (mLocData.longitude * 1E6));
		e.putString(SP_VALUE_STRING_LOCATION_CITY, mCity);
		e.commit();
	}

	private void loadLocationData() {
		mLocData.latitude = (double) (mDefualtPreferences.getInt(
				SP_VALUE_INT_LOCATION_LATITUDE, 32051176)) / 1E6;
		mLocData.longitude = (double) (mDefualtPreferences.getInt(
				SP_VALUE_INT_LOCATION_LONGITUDE, 118828577)) / 1E6;
		mCity = mDefualtPreferences.getString(mCity, "南京市");
		mLastLocation = MapUtil.getGeoPoint(mLocData);
	}
}
