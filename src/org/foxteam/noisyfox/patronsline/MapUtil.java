package org.foxteam.noisyfox.patronsline;

import com.baidu.mapapi.map.LocationData;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.graphics.Bitmap;
import android.view.View;

public class MapUtil {

	/**
	 * 从view 得到图片
	 * 
	 * @param view
	 * @return
	 */
	public static Bitmap getBitmapFromView(View view) {
		view.destroyDrawingCache();
		view.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.setDrawingCacheEnabled(true);
		Bitmap bitmap = view.getDrawingCache(true);
		return bitmap;
	}

	public static GeoPoint getGeoPoint(LocationData loc) {
		if (loc == null) {
			return null;
		}

		return new GeoPoint((int) (loc.latitude * 1E6),
				(int) (loc.longitude * 1E6));
	}

	public static int findIntegerInString(String str, int start) {
		if (str == null)
			return 0;
		int v = 0;
		boolean ne = false;
		for (int i = start; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '-') {
				if (!ne) {
					ne = true;
				} else {
					break;
				}
			} else if (Character.isDigit(c)) {
				v *= 10;
				v += Integer.parseInt(String.valueOf(c));
			} else {
				break;
			}
		}

		return ne ? -v : v;
	}

	public static String getRoughDistanceExpress(double meter) {
		int dis = (int) Math.ceil(meter);
		if (dis <= 10) {
			return "10米内";
		} else if (dis < 995) {
			int less = dis % 10;
			int h = dis - less;
			if (less >= 5) {
				h += 10;
			}
			return h + "米";
		} else {
			int less = dis % 100;
			int h = dis - less;
			if (less >= 50) {
				h += 100;
			}
			float s = h / 1000.0f;
			return String.format("%.1f千米", s);
		}

	}

	public static String removeStationFromBusName(String name) {
		if (name == null)
			return null;

		int xLoc = name.indexOf('-');
		if (xLoc == -1)
			return name;
		int kCount = 0;
		int newLoc = 0;
		for (int i = xLoc; i >= 0; i--) {
			char c = name.charAt(i);
			if (c == ')') {
				kCount++;
			} else if (c == '(') {
				if (kCount > 0) {
					kCount--;
				} else {
					newLoc = i - 1;
				}
			}
		}
		if (newLoc < 0)
			return "";

		return name.substring(0, newLoc + 1);
	}

}
