package org.foxteam.noisyfox.patronsline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

public class InformationManager {

	private static Map<String, InformationFood> mFoodMap = new HashMap<String, InformationFood>();
	private static Map<String, InformationShop> mShopMap = new HashMap<String, InformationShop>();

	public static InformationFood obtainFoodInformation(String fid) {
		if (TextUtils.isEmpty(fid)) {
			return null;
		}

		InformationFood food = null;

		if (mFoodMap.containsKey(fid)) {
			food = mFoodMap.get(fid);
		} else {
			food = new InformationFood();
			food.fid = fid;
			mFoodMap.put(fid, food);
		}

		return food;
	}

	public static InformationShop obtainShopInformation(String sid) {
		if (TextUtils.isEmpty(sid)) {
			return null;
		}

		InformationShop shop = null;

		if (mShopMap.containsKey(sid)) {
			shop = mShopMap.get(sid);
		} else {
			shop = new InformationShop();
			shop.isDetailed = false;
			shop.sid = sid;
			shop.foods = new ArrayList<InformationFood>();
			mShopMap.put(sid, shop);
		}

		return shop;
	}
}
