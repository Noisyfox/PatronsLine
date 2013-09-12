package org.foxteam.noisyfox.patronsline;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.mapapi.search.MKWpNode;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class MKSearchOperator {

	static final MKSearchOperator mInstance = new MKSearchOperator();

	private static Object mSync = new Object();

	private List<SearchRequest> mSearchRequestList = new LinkedList<SearchRequest>();
	MKSearchListener mCurrentListener = null;

	// private List<Pair<>>

	private MKSearch mMKSearch = new MKSearch();
	private MKSearchListener mMKSearchListener = new MKSearchListener() {
		@Override
		public void onGetPoiDetailSearchResult(int type, int error) {
			onResultGet();
			if (mCurrentListener != null) {
				mCurrentListener.onGetPoiDetailSearchResult(type, error);
			}
			handleNextSearchRequest();
		}

		@Override
		public void onGetPoiResult(MKPoiResult res, int type, int error) {
			onResultGet();
			if (mCurrentListener != null) {
				mCurrentListener.onGetPoiResult(res, type, error);
			}
			handleNextSearchRequest();
		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult res, int error) {
			onResultGet();
			if (mCurrentListener != null) {
				mCurrentListener.onGetDrivingRouteResult(res, error);
			}
			handleNextSearchRequest();
		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult res, int error) {
			onResultGet();
			if (mCurrentListener != null) {
				mCurrentListener.onGetTransitRouteResult(res, error);
			}
			handleNextSearchRequest();
		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult res, int error) {
			onResultGet();
			if (mCurrentListener != null) {
				mCurrentListener.onGetWalkingRouteResult(res, error);
			}
			handleNextSearchRequest();
		}

		@Override
		public void onGetAddrResult(MKAddrInfo res, int error) {
			onResultGet();
			if (mCurrentListener != null) {
				mCurrentListener.onGetAddrResult(res, error);
			}
			handleNextSearchRequest();
		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult result, int iError) {
			onResultGet();
			if (mCurrentListener != null) {
				mCurrentListener.onGetBusDetailResult(result, iError);
			}
			handleNextSearchRequest();
		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult res, int arg1) {
			onResultGet();
			if (mCurrentListener != null) {
				mCurrentListener.onGetSuggestionResult(res, arg1);
			}
			handleNextSearchRequest();
		}

		@Override
		public void onGetShareUrlResult(MKShareUrlResult result, int type,
				int error) {
			onResultGet();
			if (mCurrentListener != null) {
				mCurrentListener.onGetShareUrlResult(result, type, error);
			}
			handleNextSearchRequest();
		}
	};

	private MKSearchOperator() {
	}

	public static void init(BMapManager bmapMan) {
		if (!mInstance.mMKSearch.init(bmapMan, mInstance.mMKSearchListener)) {
			throw new RuntimeException();
		}
	}

	static final long SEARCH_TIME_OUT = 5000L;
	Timer mTimer = new Timer();
	TimerTask mTimerTask = null;
	boolean isHandleStarted = false;

	private void startHandle() {
		synchronized (mSync) {
			if (!isHandleStarted) {
				isHandleStarted = true;
				handleNextSearchRequest();
			}
		}
	}

	private void onResultGet() {
		mTimer.cancel();
		mTimer.purge();
		mTimer = new Timer();
	}

	private void handleNextSearchRequest() {
		synchronized (mSync) {
			if (!mSearchRequestList.isEmpty()) {
				mTimerTask = new TimerTask() {
					@Override
					public void run() {
						onResultGet();
						handleNextSearchRequest();
					}
				};
				mTimer.schedule(mTimerTask, SEARCH_TIME_OUT);
				SearchRequest request = mSearchRequestList.get(0);
				mSearchRequestList.remove(0);
				mCurrentListener = request.getListener();
				request.doSearch(mMKSearch);
			} else {
				isHandleStarted = false;
			}
		}
	}

	abstract class SearchRequest {
		MKSearchListener mListener = null;

		abstract void doSearch(MKSearch search);

		SearchRequest(MKSearchListener listener) {
			mListener = listener;
		}

		MKSearchListener getListener() {
			return mListener;
		}
	}

	private class RequestBusLineSearch extends SearchRequest {
		String city;
		String busLineUid;

		RequestBusLineSearch(MKSearchListener listener, String city,
				String busLineUid) {
			super(listener);
			this.city = city;
			this.busLineUid = busLineUid;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.busLineSearch(city, busLineUid);
		}

	}

	private class RequestDrivingSearch extends SearchRequest {
		String startCity;
		MKPlanNode start;
		String endCity;
		MKPlanNode end;

		RequestDrivingSearch(MKSearchListener listener, String startCity,
				MKPlanNode start, String endCity, MKPlanNode end) {
			super(listener);
			this.startCity = startCity;
			this.start = start;
			this.endCity = endCity;
			this.end = end;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.drivingSearch(startCity, start, endCity, end);
		}

	}

	private class RequestDrivingSearch2 extends SearchRequest {
		String startCity;
		MKPlanNode start;
		String endCity;
		MKPlanNode end;
		List<MKWpNode> wpNodes;

		RequestDrivingSearch2(MKSearchListener listener, String startCity,
				MKPlanNode start, String endCity, MKPlanNode end,
				List<MKWpNode> wpNodes) {
			super(listener);
			this.startCity = startCity;
			this.start = start;
			this.endCity = endCity;
			this.end = end;
			this.wpNodes = wpNodes;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.drivingSearch(startCity, start, endCity, end, wpNodes);
		}

	}

	private class RequestGeocode extends SearchRequest {
		String strAddr;
		String city;

		RequestGeocode(MKSearchListener listener, String strAddr, String city) {
			super(listener);
			this.strAddr = strAddr;
			this.city = city;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.geocode(strAddr, city);
		}

	}

	private class RequestGoToPoiPage extends SearchRequest {
		int num;

		RequestGoToPoiPage(MKSearchListener listener, int num) {
			super(listener);
			this.num = num;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.goToPoiPage(num);
		}

	}

	private class RequestPoiDetailSearch extends SearchRequest {
		String uid;

		RequestPoiDetailSearch(MKSearchListener listener, String uid) {
			super(listener);
			this.uid = uid;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.poiDetailSearch(uid);
		}

	}

	private class RequestPoiDetailShareURLSearch extends SearchRequest {
		String poiUid;

		RequestPoiDetailShareURLSearch(MKSearchListener listener, String poiUid) {
			super(listener);
			this.poiUid = poiUid;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.poiDetailShareURLSearch(poiUid);
		}

	}

	private class RequestPoiMultiSearchInbounds extends SearchRequest {
		String[] keys;
		GeoPoint ptLB;
		GeoPoint ptRT;

		RequestPoiMultiSearchInbounds(MKSearchListener listener, String[] keys,
				GeoPoint ptLB, GeoPoint ptRT) {
			super(listener);
			this.keys = keys;
			this.ptLB = ptLB;
			this.ptRT = ptRT;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.poiMultiSearchInbounds(keys, ptLB, ptRT);
		}
	}

	private class RequestPoiMultiSearchNearBy extends SearchRequest {
		String[] keys;
		GeoPoint pt;
		int radius;

		RequestPoiMultiSearchNearBy(MKSearchListener listener, String[] keys,
				GeoPoint pt, int radius) {
			super(listener);
			this.keys = keys;
			this.pt = pt;
			this.radius = radius;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.poiMultiSearchNearBy(keys, pt, radius);
		}

	}

	private class RequestPoiRGCShareURLSearch extends SearchRequest {
		GeoPoint location;
		String name;
		String address;

		RequestPoiRGCShareURLSearch(MKSearchListener listener,
				GeoPoint location, String name, String address) {
			super(listener);
			this.location = location;
			this.name = name;
			this.address = address;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.poiRGCShareURLSearch(location, name, address);
		}

	}

	private class RequestPoiSearchInbounds extends SearchRequest {
		String key;
		GeoPoint ptLB;
		GeoPoint ptRT;

		RequestPoiSearchInbounds(MKSearchListener listener, String key,
				GeoPoint ptLB, GeoPoint ptRT) {
			super(listener);
			this.key = key;
			this.ptLB = ptLB;
			this.ptRT = ptRT;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.poiSearchInbounds(key, ptLB, ptRT);
		}

	}

	private class RequestPoiSearchInCity extends SearchRequest {
		String city;
		String key;

		RequestPoiSearchInCity(MKSearchListener listener, String city,
				String key) {
			super(listener);
			this.city = city;
			this.key = key;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.poiSearchInCity(city, key);
		}

	}

	private class RequestPoiSearchNearBy extends SearchRequest {
		String key;
		GeoPoint pt;
		int radius;

		RequestPoiSearchNearBy(MKSearchListener listener, String key,
				GeoPoint pt, int radius) {
			super(listener);
			this.key = key;
			this.pt = pt;
			this.radius = radius;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.poiSearchNearBy(key, pt, radius);
		}

	}

	private class RequestReverseGeocode extends SearchRequest {
		GeoPoint pt;

		RequestReverseGeocode(MKSearchListener listener, GeoPoint pt) {
			super(listener);
			this.pt = pt;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.reverseGeocode(pt);
		}

	}

	private class RequestSuggestionSearch extends SearchRequest {
		String key;
		String city;

		RequestSuggestionSearch(MKSearchListener listener, String key,
				String city) {
			super(listener);
			this.key = key;
			this.city = city;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.suggestionSearch(key, city);
		}

	}

	private class RequestTransitSearch extends SearchRequest {
		String city;
		MKPlanNode start;
		MKPlanNode end;

		RequestTransitSearch(MKSearchListener listener, String city,
				MKPlanNode start, MKPlanNode end) {
			super(listener);
			this.city = city;
			this.start = start;
			this.end = end;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.transitSearch(city, start, end);
		}

	}

	private class RequestWalkingSearch extends SearchRequest {
		String startCity;
		MKPlanNode start;
		String endCity;
		MKPlanNode end;

		RequestWalkingSearch(MKSearchListener listener, String startCity,
				MKPlanNode start, String endCity, MKPlanNode end) {
			super(listener);
			this.startCity = startCity;
			this.start = start;
			this.endCity = endCity;
			this.end = end;
		}

		@Override
		public void doSearch(MKSearch search) {
			search.walkingSearch(startCity, start, endCity, end);
		}

	}

	static void busLineSearch(MKSearchListener listener, String city,
			String busLineUid) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestBusLineSearch(listener, city,
							busLineUid));
			mInstance.startHandle();
		}
	}

	static void drivingSearch(MKSearchListener listener, String startCity,
			MKPlanNode start, String endCity, MKPlanNode end) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestDrivingSearch(listener,
							startCity, start, endCity, end));
			mInstance.startHandle();
		}
	}

	static void drivingSearch(MKSearchListener listener, String startCity,
			MKPlanNode start, String endCity, MKPlanNode end,
			List<MKWpNode> wpNodes) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestDrivingSearch2(listener,
							startCity, start, endCity, end, wpNodes));
			mInstance.startHandle();
		}
	}

	static void geocode(MKSearchListener listener, String strAddr, String city) {
		synchronized (mSync) {
			mInstance.mSearchRequestList.add(mInstance.new RequestGeocode(
					listener, strAddr, city));
			mInstance.startHandle();
		}
	}

	static void goToPoiPage(MKSearchListener listener, int num) {
		synchronized (mSync) {
			mInstance.mSearchRequestList.add(mInstance.new RequestGoToPoiPage(
					listener, num));
			mInstance.startHandle();
		}
	}

	static void poiDetailSearch(MKSearchListener listener, String uid) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestPoiDetailSearch(listener, uid));
			mInstance.startHandle();
		}
	}

	static void poiDetailShareURLSearch(MKSearchListener listener, String poiUid) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestPoiDetailShareURLSearch(listener,
							poiUid));
			mInstance.startHandle();
		}
	}

	static void poiMultiSearchInbounds(MKSearchListener listener,
			String[] keys, GeoPoint ptLB, GeoPoint ptRT) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestPoiMultiSearchInbounds(listener,
							keys, ptLB, ptRT));
			mInstance.startHandle();
		}
	}

	static void poiMultiSearchNearBy(MKSearchListener listener, String[] keys,
			GeoPoint pt, int radius) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestPoiMultiSearchNearBy(listener,
							keys, pt, radius));
			mInstance.startHandle();
		}
	}

	static void poiRGCShareURLSearch(MKSearchListener listener,
			GeoPoint location, String name, String address) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestPoiRGCShareURLSearch(listener,
							location, name, address));
			mInstance.startHandle();
		}
	}

	static void poiSearchInbounds(MKSearchListener listener, String key,
			GeoPoint ptLB, GeoPoint ptRT) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestPoiSearchInbounds(listener, key,
							ptLB, ptRT));
			mInstance.startHandle();
		}
	}

	static void poiSearchInCity(MKSearchListener listener, String city,
			String key) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestPoiSearchInCity(listener, city,
							key));
			mInstance.startHandle();
		}
	}

	static void poiSearchNearBy(MKSearchListener listener, String key,
			GeoPoint pt, int radius) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestPoiSearchNearBy(listener, key,
							pt, radius));
			mInstance.startHandle();
		}
	}

	static void reverseGeocode(MKSearchListener listener, GeoPoint pt) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestReverseGeocode(listener, pt));
			mInstance.startHandle();
		}
	}

	static void suggestionSearch(MKSearchListener listener, String key,
			String city) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestSuggestionSearch(listener, key,
							city));
			mInstance.startHandle();
		}
	}

	static void transitSearch(MKSearchListener listener, String city,
			MKPlanNode start, MKPlanNode end) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestTransitSearch(listener, city,
							start, end));
			mInstance.startHandle();
		}
	}

	static void walkingSearch(MKSearchListener listener, String startCity,
			MKPlanNode start, String endCity, MKPlanNode end) {
		synchronized (mSync) {
			mInstance.mSearchRequestList
					.add(mInstance.new RequestWalkingSearch(listener,
							startCity, start, endCity, end));
			mInstance.startHandle();
		}
	}
}
