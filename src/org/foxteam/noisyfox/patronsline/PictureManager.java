package org.foxteam.noisyfox.patronsline;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Pair;

/**
 * 一个用来管理所有图片的类，负责根据图片id获取bitmap.<br>
 * 可以从网络、cache两种渠道获取.
 * 
 * @author noisyfox
 * 
 */
public class PictureManager {

	private static Map<String, Bitmap> mPictureCache = new HashMap<String, Bitmap>();

	private OnPictureGetListener mOnPictureGetListener = null;

	public void setOnPictureGetListener(OnPictureGetListener listener) {
		mOnPictureGetListener = listener;
	}

	public OnPictureGetListener getOnPictureGetListener() {
		return mOnPictureGetListener;
	}

	public void getPicture(String pid) {
		synchronized (mPictureCache) {
			if (mPictureCache.containsKey(pid)) {
				onPicGet(pid, mPictureCache.get(pid));
			} else {
				new PicLoadTask().execute(pid);
			}
		}
	}

	private void onPicGet(String pid, Bitmap pic) {
		if (mOnPictureGetListener != null) {
			mOnPictureGetListener.onPictureGet(pid, pic);
		}
	}

	class PicLoadTask extends AsyncTask<String, Void, Pair<String, Bitmap>> {

		@Override
		protected Pair<String, Bitmap> doInBackground(String... params) {
			String pid = params[0];
			try {
				List<BasicNameValuePair> postData = new ArrayList<BasicNameValuePair>();
				postData.add(new BasicNameValuePair("method", "image"));
				postData.add(new BasicNameValuePair("id", pid));

				DefaultHttpClient httpClient = new DefaultHttpClient();
				// Represents a collection of HTTP protocol and framework
				// parameters
				HttpParams httpParams = httpClient.getParams();
				// 设置超时
				HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
				HttpConnectionParams.setSoTimeout(httpParams, 35000);

				HttpPost post = new HttpPost(NetworkHelper.STR_SERVER_URL);
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
						postData, HTTP.UTF_8);
				post.setEntity(entity);

				HttpResponse response = httpClient.execute(post);

				if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
					return null;

				HttpEntity httpEntity = response.getEntity();
				InputStream is = httpEntity.getContent();
				Bitmap image = BitmapFactory.decodeStream(is);

				return new Pair<String, Bitmap>(pid, image);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Pair<String, Bitmap> result) {
			synchronized (mPictureCache) {
				if (result == null) {
					if (mOnPictureGetListener != null) {
						mOnPictureGetListener.onError();
					}
				} else {
					if (!mPictureCache.containsKey(result.first)) {
						mPictureCache.put(result.first, result.second);
						onPicGet(result.first, result.second);
					} else {
						onPicGet(result.first, mPictureCache.get(result.first));
					}
				}
			}
		}

	}

}