package org.foxteam.noisyfox.patronsline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

public class NetworkHelper {
	static final String STR_SERVER_URL = "http://192.168.0.13/tyserver/tyapp.php";

	public static String doHttpRequest(String url,
			Set<Entry<Object, Object>> data) {

		try {
			List<BasicNameValuePair> postData = new ArrayList<BasicNameValuePair>();
			for (Map.Entry<Object, Object> entry : data) {
				postData.add(new BasicNameValuePair(entry.getKey().toString(),
						entry.getValue().toString()));
			}

			DefaultHttpClient httpClient = new DefaultHttpClient();
			// Represents a collection of HTTP protocol and framework parameters
			HttpParams params = null;
			params = httpClient.getParams();
			// 设置超时
			HttpConnectionParams.setConnectionTimeout(params, 5000);
			HttpConnectionParams.setSoTimeout(params, 35000);

			HttpPost post = new HttpPost(url);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postData,
					HTTP.UTF_8);
			post.setEntity(entity);

			HttpResponse response = httpClient.execute(post);

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				return null;

			HttpEntity httpEntity = response.getEntity();
			InputStream is = httpEntity.getContent();
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			return sb.toString();
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

}
