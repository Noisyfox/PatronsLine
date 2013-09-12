package org.foxteam.noisyfox.patronsline;

import com.actionbarsherlock.app.SherlockActivity;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ConsumerNavigateActivity extends SherlockActivity {
	public static final String STR_SHOPADDRESS = "shopAddress";
	private String mShopAddress = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_consumer_navigate);

		mShopAddress = getIntent().getStringExtra(STR_SHOPADDRESS);

		mMyApp = (PatronsLineApplication) this.getApplication();

		mMyApp.initEngineManager(this);// 确保初始化
		mMyApp.initLocationClient(this);

		mProgressView = findViewById(R.id.map_load_process);
		mContentView = findViewById(R.id.map_content);
		mProgressTextView = (Button) findViewById(R.id.textView_progress);
		requestLocButton = (Button) findViewById(R.id.button_relocate);
		mZoomInButton = (Button) findViewById(R.id.button_zoom_in);
		mZoomOutButton = (Button) findViewById(R.id.button_zoom_out);
		requestLocButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 手动定位请求
				requestLocClick();
			}
		});
		mZoomInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mZoomOutButton.setEnabled(true);
				if (!mMapView.getController().zoomIn()) {
					mZoomInButton.setEnabled(false);
				}
			}
		});
		mZoomOutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mZoomInButton.setEnabled(true);
				if (!mMapView.getController().zoomOut()) {
					mZoomOutButton.setEnabled(false);
				}
			}
		});

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapController = mMapView.getController();
		mMapController.setZoom(14);
		mMapController.enableClick(true);
		LocationData ld = mMyApp.getLocData();
		mMapController.setCenter(new GeoPoint((int) (ld.latitude * 1e6),
				(int) (ld.longitude * 1e6)));
		mMapView.regMapViewListener(mMyApp.getMapManager(),
				new MyMapViewListener());
		// 定位初始化
		LocationClient locClient = mMyApp.getLocClient();
		LocationData locData = mMyApp.getLocData();
		mMyApp.registerLocationListener(myListener);
		locClient.start();

		// 定位图层初始化
		myLocationOverlay = new MyLocationOverlay(mMapView);
		// 设置定位数据
		myLocationOverlay.setData(locData);
		// 添加定位图层
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		// 修改定位数据后刷新图层生效
		mMapView.refresh();

		showProgress(true);
	}

	void showProgress(boolean show) {
		mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
		mContentView.setVisibility(!show ? View.VISIBLE : View.GONE);
	}

	// 定位相关
	// LocationClient mLocClient;
	// LocationData locData = null;
	PatronsLineApplication mMyApp = null;

	public MyLocationListener myListener = new MyLocationListener() {

		@Override
		public void onLocationChanged(LocationData locationData) {
			// 更新定位数据
			myLocationOverlay.setData(locationData);
			// 更新图层数据执行刷新后生效
			mMapView.refresh();
			// 是手动触发请求或首次定位时，移动到定位点
			if (isRequest) {
				// 移动地图到定位点
				mMapController.animateTo(new GeoPoint(
						(int) (locationData.latitude * 1e6),
						(int) (locationData.longitude * 1e6)));
				isRequest = false;
			}
			// 首次定位完成
			if(isFirstLoc){
				// 移动地图到定位点
				mMapController.animateTo(new GeoPoint(
						(int) (locationData.latitude * 1e6),
						(int) (locationData.longitude * 1e6)));
				//搜索指定地址
				
			}
			isFirstLoc = false;
		}

		@Override
		public void onCityChanged(String city) {
		}

	};
	// 定位图层
	MyLocationOverlay myLocationOverlay = null;

	// 地图相关，使用继承MapView的MyLocationMapView目的是重写touch事件实现泡泡处理
	// 如果不处理touch事件，则无需继承，直接使用MapView即可
	private MapController mMapController = null;

	// UI相关
	OnCheckedChangeListener radioButtonListener = null;
	boolean isRequest = false;// 是否手动触发请求定位
	boolean isFirstLoc = true;// 是否首次定位

	MapView mMapView = null; // 地图View
	Button requestLocButton = null;
	Button mZoomInButton = null;
	Button mZoomOutButton = null;
	TextView mProgressTextView = null;
	View mProgressView = null;
	View mContentView = null;

	// 地图事件监听类
	public class MyMapViewListener implements MKMapViewListener {

		@Override
		public void onClickMapPoi(MapPoi arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetCurrentMap(Bitmap arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMapAnimationFinish() {
			float zoomLevel = mMapView.getZoomLevel();
			mZoomOutButton.setEnabled(zoomLevel > 3);
			mZoomInButton.setEnabled(zoomLevel < 19);
		}

		@Override
		public void onMapLoadFinish() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMapMoveFinish() {
			float zoomLevel = mMapView.getZoomLevel();
			mZoomOutButton.setEnabled(zoomLevel > 3);
			mZoomInButton.setEnabled(zoomLevel < 19);

		}

	}

	/**
	 * 手动触发一次定位请求
	 */
	public void requestLocClick() {
		isRequest = true;
		mMyApp.getLocClient().requestLocation();
		Toast.makeText(this, "正在定位……", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPause() {
		mMyApp.unregisterLocationListener(myListener);
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMyApp.registerLocationListener(myListener);
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		// mMyApp.destroyLocationClient();
		mMapView.destroy();
		mMyApp.getLocClient().stop();
		// mMyApp.destroyEngineManager();
		super.onDestroy();
		// System.exit(0);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mMapView.onRestoreInstanceState(savedInstanceState);
	}
}
