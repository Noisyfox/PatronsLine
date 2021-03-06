package org.foxteam.noisyfox.patronsline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;

/**
 * 一个用来管理所有图片的类，负责根据图片id获取bitmap.<br>
 * 可以从网络、cache两种渠道获取.
 * 
 * @author noisyfox
 * 
 */
public class PictureManager {

	public final static String PATH_CACHE_FOLDER = PatronsLineApplication.PATH_APP_WORKDIR
			+ "/cache/pic";

	private final static Map<String, Bitmap> mPictureCache = new HashMap<String, Bitmap>();

	private OnPictureGetListener mOnPictureGetListener = null;

	// 从缓存中读取指定pid的图片资源
	public boolean cacheLoad(String pid) {
		synchronized (mPictureCache) {
			if (mPictureCache.containsKey(pid)) {
				return true;
			} else {
				// 检查缓存文件夹中有没有这个图片
				if (isHasSdcard()) {
					File f = new File(PATH_CACHE_FOLDER + "/" + pid + ".jpg");
					if (f.exists()) {
						try {
							Bitmap bmp = loadBitmap(f.getAbsolutePath());
							mPictureCache.put(pid, bmp);
							Log.d("picMgr", "Load cache:" + pid);
							return true;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return false;
	}

	public static void cacheSave() {
		synchronized (mPictureCache) {
			Set<Entry<String, Bitmap>> set = mPictureCache.entrySet();
			File dir = new File(PATH_CACHE_FOLDER);
			dir.mkdirs();
			for (Entry<String, Bitmap> entry : set) {
				String pid = entry.getKey();
				File f = new File(PATH_CACHE_FOLDER + "/" + pid + ".jpg");
				if (!f.exists()) {
					try {
						saveBitmap(entry.getValue(), f.getAbsolutePath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void setOnPictureGetListener(OnPictureGetListener listener) {
		mOnPictureGetListener = listener;
	}

	public OnPictureGetListener getOnPictureGetListener() {
		return mOnPictureGetListener;
	}

	public void getPicture(String pid) {
		synchronized (mPictureCache) {
			if (cacheLoad(pid)) {
				onPicGet(pid, mPictureCache.get(pid));
			} else {
				new PicLoadTask().execute(pid);
			}
		}
	}

	private void onPicGet(String pid, Bitmap pic) {
		Log.d("picMgr", "Pic get:" + pid);
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

				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity httpEntity = response.getEntity();
					InputStream is = httpEntity.getContent();
					Bitmap image = BitmapFactory.decodeStream(is);

					return new Pair<String, Bitmap>(pid, image);
				}
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

			return new Pair<String, Bitmap>(pid, null);
		}

		@Override
		protected void onPostExecute(Pair<String, Bitmap> result) {
			synchronized (mPictureCache) {
				if (result.second != null) {
					if (!mPictureCache.containsKey(result.first)) {
						mPictureCache.put(result.first, result.second);
						onPicGet(result.first, result.second);
					} else {
						onPicGet(result.first, mPictureCache.get(result.first));
					}
				} else {
					onPicGet(result.first, result.second);
				}
			}
		}

	}

	public static Bitmap loadBitmap(String filePath) throws IOException {
		File f = new File(filePath);
		FileInputStream fIn = new FileInputStream(f);

		Bitmap image = BitmapFactory.decodeStream(fIn);

		fIn.close();

		return image;
	}

	public static void saveBitmap(Bitmap bitmap, String filePath)
			throws IOException {
		File f = new File(filePath);
		f.createNewFile();
		FileOutputStream fOut = new FileOutputStream(f);

		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

		try {
			fOut.flush();
		} finally {
			fOut.close();
		}

	}

	public static boolean isHasSdcard() {
		String status = Environment.getExternalStorageState();
		return status.equals(Environment.MEDIA_MOUNTED);
	}

	public static interface OnPictureGetListener {
		public void onPictureGet(String pid, Bitmap pic);
	}

	public static void loadPicture(final InformationFood food,
			final ImageView imageView) {
		if (food.photoBitmap != null) {
			imageView.setImageBitmap(food.photoBitmap);
		} else {
			PictureManager pm = new PictureManager();
			pm.setOnPictureGetListener(new OnPictureGetListener() {
				@Override
				public void onPictureGet(String pid, Bitmap pic) {
					if (pid == food.photo && pic != null) {
						food.photoBitmap = pic;
						imageView.setImageBitmap(food.photoBitmap);
					}
				}
			});
			pm.getPicture(food.photo);
		}
	}

	public static void loadPicture(final InformationShop shop,
			final ImageView imageView) {
		if (shop.photoBitmap != null) {
			imageView.setImageBitmap(shop.photoBitmap);
		} else {
			PictureManager pm = new PictureManager();
			pm.setOnPictureGetListener(new OnPictureGetListener() {
				@Override
				public void onPictureGet(String pid, Bitmap pic) {
					if (pid == shop.photo && pic != null) {
						shop.photoBitmap = pic;
						imageView.setImageBitmap(shop.photoBitmap);
					}
				}
			});
			pm.getPicture(shop.photo);
		}
	}
}
