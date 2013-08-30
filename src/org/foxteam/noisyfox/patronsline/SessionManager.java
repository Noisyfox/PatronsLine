package org.foxteam.noisyfox.patronsline;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.foxteam.noisyfox.patronsline.PictureManager.OnPictureGetListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * 用户session管理器，负责用户的登陆、信息的获取
 * 
 * @author noisyfox
 * 
 */
public class SessionManager {
	public static final int ACTION_USER_REGISTER = 1;
	public static final int ACTION_USER_LOGIN = 2;

	public static final int ERROR_OK = 0;
	public static final int ERROR_IO_FAILURE = 1;
	public static final int ERROR_NETWORK_FAILURE = 2;
	public static final int ERROR_SERVER_FAILURE = 3;
	public static final int ERROR_USER_NAME_DUPLICATE = 4;
	public static final int ERROR_USER_LOGIN_FAILURE = 5;
	public static final int ERROR_INTERNAL = 6;

	private InformationSession mSession;

	private static SessionManager mSessionManager = new SessionManager();

	private SessionManager() {

	}

	public static SessionManager getSessionManager() {
		return mSessionManager;
	}

	public static InformationSession getCurrentSession() {
		return mSessionManager.mSession;
	}

	public int user_register(String name, String psw, int sex, int type,
			Bitmap avatar, String school, String region) {
		Map<Object, Object> params = new HashMap<Object, Object>();

		params.put("method", "user.register");
		params.put("name", name);
		params.put("password", hashString(psw, "SHA"));
		params.put("sex", sex);
		params.put("type", type);
		params.put("school", school);
		params.put("region", region);
		ImageUploader.Image img = null;
		try {
			img = new ImageUploader.Image("avatar", "headUpload_" + name,
					avatar);
		} catch (IOException e) {
			e.printStackTrace();
			return ERROR_IO_FAILURE;
		}

		String response = ImageUploader.post(NetworkHelper.STR_SERVER_URL,
				params.entrySet(), new ImageUploader.Image[] { img });

		if (response == null) {
			return ERROR_NETWORK_FAILURE;
		}

		Log.d("session", response);

		try {
			JSONTokener jsonParser = new JSONTokener(response);
			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
			int result = jsonObj.getInt("result");
			switch (result) {
			case 1:// OK
				break;
			case -1:// 用户名重复
				return ERROR_USER_NAME_DUPLICATE;
			case 0:// 服务器错误
				return ERROR_SERVER_FAILURE;
			}
			String uid = jsonObj.getString("uid");
			String session = jsonObj.getString("session");

			InformationSession tmpSession = new InformationSession();
			tmpSession.uid = uid;
			tmpSession.session = session;

			mSession = tmpSession;

			return ERROR_OK;
		} catch (JSONException e) {
			e.printStackTrace();
			return ERROR_SERVER_FAILURE;
		}

	}

	private int loginUser(Map<Object, Object> params) {
		String response = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL, params.entrySet());

		if (response == null) {
			return ERROR_NETWORK_FAILURE;
		}
		Log.d("session", response);

		try {
			JSONTokener jsonParser = new JSONTokener(response);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}

			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
			int result = jsonObj.getInt("result");

			switch (result) {
			case 1:// OK
				break;
			case -1:// 登陆失败
				return ERROR_USER_LOGIN_FAILURE;
			case 0:// 服务器错误
				return ERROR_SERVER_FAILURE;
			}

			// 登陆成功
			String uid = jsonObj.getString("uid");
			String session = jsonObj.getString("session");
			String name = jsonObj.getString("name");
			int sex = jsonObj.getInt("sex");
			int type = jsonObj.getInt("type");
			String avatar = jsonObj.getString("avatar");
			String school = jsonObj.getString("school");
			String region = jsonObj.getString("region");

			InformationSession tmpSession = new InformationSession();
			InformationUser tmpUser = new InformationUser();
			tmpSession.uid = uid;
			tmpSession.session = session;
			tmpSession.user = tmpUser;
			tmpUser.name = name;
			tmpUser.sex = sex;
			tmpUser.type = type;
			tmpUser.avatar = avatar;
			tmpUser.school = school;
			tmpUser.region = region;
			if (type == 1) {
				String shopid = jsonObj.getString("shopid");
				tmpUser.shopid = new ArrayList<String>();
				tmpUser.shopid.add(shopid);
				tmpSession.ownedShop = new ArrayList<InformationShop>();
			} else {
				tmpSession.bookmarkFood = new ArrayList<InformationBookmarkFood>();
				tmpSession.bookmarkShop = new ArrayList<InformationBookmarkShop>();
			}

			mSession = tmpSession;

			PictureManager pictureManager = new PictureManager();
			pictureManager.setOnPictureGetListener(new OnPictureGetListener() {
				@Override
				public void onPictureGet(String pid, Bitmap pic) {
					if (pic != null && mSession != null
							&& mSession.user != null
							&& mSession.user.avatar == pid) {
						mSession.user.avatarBitmap = pic;
					}
				}
			});
			pictureManager.getPicture(avatar);

			return ERROR_OK;

		} catch (JSONException e) {
			e.printStackTrace();
			return ERROR_SERVER_FAILURE;
		}
	}

	public int user_login(InformationSession userSession) {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("method", "user.login");
		params.put("uid", userSession.uid);
		params.put("session", userSession.session);

		return loginUser(params);
	}

	public int user_login(String userName, String psw) {
		String psw_hash = hashString(psw, "SHA");
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("method", "user.login");
		params.put("name", userName);
		params.put("password", psw_hash);

		return loginUser(params);
	}

	public int bookmark_list_food() {
		if (mSession.user.type != 0) {
			return ERROR_INTERNAL;
		}

		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("method", "bookmark.list");
		params.put("uid", mSession.uid);
		params.put("session", mSession.session);
		params.put("type", "food");

		String response = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL, params.entrySet());

		if (response == null) {
			return ERROR_NETWORK_FAILURE;
		}
		Log.d("session", response);

		try {
			JSONTokener jsonParser = new JSONTokener(response);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}

			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
			int result = jsonObj.getInt("result");

			switch (result) {
			case 1:// OK
				break;
			default:// 服务器错误
				return ERROR_SERVER_FAILURE;
			}

			mSession.bookmarkFood.clear();

			JSONArray jsnonArray = jsonObj.getJSONArray("bookmarks");
			int count = jsnonArray.length();
			for (int i = 0; i < count; i++) {
				JSONObject food = jsnonArray.getJSONObject(i);
				InformationBookmarkFood bookmark = new InformationBookmarkFood();
				String fid = food.getString("fid");
				String sid = food.getString("sid");
				bookmark.food = InformationManager.obtainFoodInformation(fid);
				bookmark.shop = InformationManager.obtainShopInformation(sid);
				mSession.bookmarkFood.add(bookmark);

				bookmark.bfid = food.getString("id");
				bookmark.food.fid = fid;
				bookmark.food.name = food.getString("name");
				bookmark.food.price = (float) food.getDouble("price");
				bookmark.food.special = food.getBoolean("special");
				bookmark.food.photo = food.getString("photo");
				bookmark.shop.sid = sid;
				bookmark.shop.name = food.getString("shopname");
			}

			return ERROR_OK;

		} catch (JSONException e) {
			e.printStackTrace();
			return ERROR_SERVER_FAILURE;
		}
	}

	public int bookmark_add(String id, boolean isFood) {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("method", "bookmark.add");
		params.put("uid", mSession.uid);
		params.put("session", mSession.session);
		params.put("id", id);
		params.put("type", isFood ? "food" : "shop");

		String response = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL, params.entrySet());

		if (response == null) {
			return ERROR_NETWORK_FAILURE;
		}
		Log.d("session", response);

		try {
			JSONTokener jsonParser = new JSONTokener(response);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}

			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
			int result = jsonObj.getInt("result");

			switch (result) {
			case 1:// OK
				return ERROR_OK;
			default:// 服务器错误
				return ERROR_SERVER_FAILURE;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			return ERROR_SERVER_FAILURE;
		}
	}

	public int bookmark_delete(String id, boolean isFood) {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put("method", "bookmark.delete");
		params.put("uid", mSession.uid);
		params.put("session", mSession.session);
		params.put("id", id);
		params.put("type", isFood ? "food" : "shop");

		String response = NetworkHelper.doHttpRequest(
				NetworkHelper.STR_SERVER_URL, params.entrySet());

		if (response == null) {
			return ERROR_NETWORK_FAILURE;
		}
		Log.d("session", response);

		try {
			JSONTokener jsonParser = new JSONTokener(response);

			jsonParser.nextTo('{');
			if (!jsonParser.more()) {
				throw new JSONException("Failed to read return value.");
			}

			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
			int result = jsonObj.getInt("result");

			switch (result) {
			case 1:// OK
				return ERROR_OK;
			default:// 服务器错误
				return ERROR_SERVER_FAILURE;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			return ERROR_SERVER_FAILURE;
		}
	}

	private static String hashString(String data, String algorithm) {
		if (data == null)
			return null;
		try {
			MessageDigest mdInst = MessageDigest.getInstance(algorithm);
			byte btInput[] = data.getBytes();
			mdInst.update(btInput);
			byte md[] = mdInst.digest();
			StringBuilder sb = new StringBuilder(64);
			for (byte b : md) {
				sb.append(String.format("%02X", b));
			}

			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
