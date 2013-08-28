package org.foxteam.noisyfox.patronsline;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * 用户session管理器，负责用户的登陆、信息的获取
 * 
 * @author noisyfox
 * 
 */
public class SessionManager {
	public static int ACTION_USER_REGISTER = 1;

	InformationSession mSession;
	OnSessionActionFinishedListener mOnSessionActionFinishedListener;

	public void setOnSessionActionFinishedListener(
			OnSessionActionFinishedListener listener) {
		mOnSessionActionFinishedListener = listener;
	}

	public void user_register(String name, String psw, int sex, int type,
			Bitmap avatar, String school, String region) {
		Map<Object, Object> params = new HashMap<Object, Object>();

		params.put("method", "user.register");
		params.put("name", name);
		params.put("password", psw);
		params.put("sex", sex);
		params.put("type", type);
		params.put("school", school);
		params.put("region", region);
		ImageUploader.Image img = null;
		try {
			img = new ImageUploader.Image("avatar", "aaa", avatar);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (mOnSessionActionFinishedListener != null) {
				mOnSessionActionFinishedListener.onSessionActionFinished(null,
						ACTION_USER_REGISTER, 1);
				return;
			}
		}

		String result = ImageUploader.post("", params.entrySet(),
				new ImageUploader.Image[] { img });

		Log.d("session", result);

	}

	public static interface OnSessionActionFinishedListener {
		public void onSessionActionFinished(InformationSession session,
				int action, int errCode);
	}
}
