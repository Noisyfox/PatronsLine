package org.foxteam.noisyfox.patronsline;

import com.actionbarsherlock.app.SherlockActivity;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ConsumerNavigateActivity extends SherlockActivity {
	public static final String STR_SHOPNAME = "shopName";
	public static final String STR_SHOPADDRESS = "shopAddress";
	private String mShopAddress = null;
	private String mShopName = null;
	private int searchType = 0;// 0--Address+" "+Name 1--Address 2--Name

	private MKSearchListener mMKSearchListener = new MKSearchListener() {

		@Override
		public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetPoiDetailSearchResult(int arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetPoiResult(MKPoiResult res, int type, int error) {
			if (error != 0 || res == null || res.getCurrentNumPois() <= 0) {
				if (searchType < 2) {
					searchType++;
					switch (searchType) {
					case 1:
						MKSearchOperator.poiSearchInCity(mMKSearchListener,
								mMyApp.getCity(), mShopAddress);
						break;
					case 2:
						MKSearchOperator.poiSearchInCity(mMKSearchListener,
								mMyApp.getCity(), mShopName);
						break;
					}
				} else {
					Toast.makeText(ConsumerNavigateActivity.this, "抱歉，未找到结果",
							Toast.LENGTH_LONG).show();
				}
				return;
			}

			for (MKPoiInfo info : res.getAllPoi()) {
				Log.d("name", info.ePoiType + ":" + info.name);
				if (info.ePoiType == 1) {
					Log.d("name", "  " + info.address);
				}
			}

			MKPoiInfo shop = res.getAllPoi().get(0);

			MKPlanNode stNode = new MKPlanNode();
			stNode.pt = MapUtil.getGeoPoint(mMyApp.getLocData());
			MKPlanNode enNode = new MKPlanNode();
			enNode.pt = shop.pt;

			String city = mMyApp.getCity();
			MKSearchOperator.walkingSearch(mMKSearchListener, city, stNode,
					city, enNode);
		}

		@Override
		public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1,
				int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult res, int error) {
			if (error != 0 || res == null) {
				Toast.makeText(ConsumerNavigateActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (routeOverlay != null) {
				mMapView.getOverlays().remove(routeOverlay);
			}
			routeOverlay = new RouteOverlay(ConsumerNavigateActivity.this,
					mMapView);
			route = res.getPlan(0).getRoute(0);
			routeOverlay.setData(route);
			mMapView.getOverlays().add(routeOverlay);
			mMapView.refresh();

			showProgress(false);
			mMapController.zoomToSpan(routeOverlay.getLatSpanE6(),
					routeOverlay.getLonSpanE6());
			// mMapController.animateTo(res.getStart().pt);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMyApp = (PatronsLineApplication) this.getApplication();
		mMyApp.initEngineManager();// 确保初始化
		mMyApp.initLocationClient();

		setContentView(R.layout.activity_consumer_navigate);

		mShopAddress = getIntent().getStringExtra(STR_SHOPADDRESS);
		mShopName = getIntent().getStringExtra(STR_SHOPNAME);

		mProgressView = findViewById(R.id.map_load_process);
		mContentView = findViewById(R.id.map_content);
		mProgressTextView = (TextView) findViewById(R.id.textView_progress);
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

		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapController = mMapView.getController();
		mMapController.setZoom(18);
		mMapController.enableClick(true);

		// 定位图层初始化
		myLocationOverlay = new MyLocationOverlay(mMapView);
		// 设置定位数据
		myLocationOverlay.setData(mMyApp.getLocData());
		// 添加定位图层
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		// 修改定位数据后刷新图层生效
		mMapView.refresh();

		// 定位初始化
		LocationClient locClient = mMyApp.getLocClient();
		mMyApp.registerLocationListener(myListener);
		locClient.start();

		showProgress(true);
	}

	void showProgress(boolean show) {
		mProgressTextView.setText(R.string.label_load_navigate_data);
		mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
		mContentView.setVisibility(!show ? View.VISIBLE : View.GONE);
		if (!show) {
			if (!isMapViewInited) {
				isMapViewInited = true;

				// 地图初始化
				LocationData ld = mMyApp.getLocData();
				mMapController.setCenter(new GeoPoint(
						(int) (ld.latitude * 1e6), (int) (ld.longitude * 1e6)));
				mMapView.regMapViewListener(mMyApp.getMapManager(),
						new MyMapViewListener());

			}
			// 移动地图到定位点
			LocationData ld = mMyApp.getLocData();
			mMapController.animateTo(new GeoPoint((int) (ld.latitude * 1e6),
					(int) (ld.longitude * 1e6)));
		}
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
			if (isFirstLoc) {
				// 搜索指定地址
				searchType = 0;
				MKSearchOperator.poiSearchInCity(mMKSearchListener,
						mMyApp.getCity(), mShopAddress + " " + mShopName);
			}
			isFirstLoc = false;
		}

		@Override
		public void onCityChanged(String city) {
		}

	};
	// 定位图层
	MyLocationOverlay myLocationOverlay = null;
	// 路线图层
	RouteOverlay routeOverlay = null;
	MKRoute route = null;// 保存驾车/步行路线数据的变量，供浏览节点时使用

	private MapController mMapController = null;

	// UI相关
	OnCheckedChangeListener radioButtonListener = null;
	boolean isRequest = false;// 是否手动触发请求定位
	boolean isFirstLoc = true;// 是否首次定位
	boolean isMapViewInited = false;

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
		LocationClient lc = mMyApp.getLocClient();
		if (lc != null) {
			lc.stop();
		}
		mMapView.destroy();
		super.onDestroy();
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
