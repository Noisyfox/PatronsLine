package org.foxteam.noisyfox.patronsline;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Map;
import java.util.Set;

import android.graphics.Bitmap;

public class ImageUploader {
	public static String PATH_TEMP_FOLDER = PatronsLineApplication.PATH_APP_WORKDIR
			+ "/tmp/upload";

	private static String multipart_form_data = "multipart/form-data";
	private static String twoHyphens = "--";
	private static String boundary = "****************fD4fH3gL0hK7aI6"; // 数据分隔符
	private static String lineEnd = System.getProperty("line.separator"); // The
																			// value
																			// is

	// "\r\n" in
	// Windows.

	/*
	 * 上传图片内容，格式请参考HTTP 协议格式。
	 * 人人网Photos.upload中的”程序调用“http://wiki.dev.renren.com/
	 * wiki/Photos.upload#.E7.A8.8B.E5.BA.8F.E8.B0.83.E7.94.A8 对其格式解释的非常清晰。
	 * 格式如下所示： --****************fD4fH3hK7aI6 Content-Disposition: form-data;
	 * name="upload_file"; filename="apple.jpg" Content-Type: image/jpeg
	 * 
	 * 这儿是文件的内容，二进制流的形式
	 */
	private static void addImageContent(Image[] files, DataOutputStream output) {
		for (Image file : files) {
			StringBuilder split = new StringBuilder();
			split.append(twoHyphens + boundary + lineEnd);
			split.append("Content-Disposition: form-data; name=\""
					+ file.getFormName() + "\"; filename=\""
					+ file.getFileName() + "\"" + lineEnd);
			split.append("Content-Type: " + file.getContentType() + lineEnd);
			split.append(lineEnd);
			try {
				// 发送图片数据
				output.writeBytes(split.toString());
				output.write(file.getData(), 0, file.getData().length);
				output.writeBytes(lineEnd);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/*
	 * 构建表单字段内容，格式请参考HTTP 协议格式（用FireBug可以抓取到相关数据）。(以便上传表单相对应的参数值) 格式如下所示：
	 * --****************fD4fH3hK7aI6 Content-Disposition: form-data;
	 * name="action" // 一空行，必须有 upload
	 */
	private static void addFormField(Set<Map.Entry<Object, Object>> params,
			DataOutputStream output) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Object, Object> param : params) {
			sb.append(twoHyphens + boundary + lineEnd);
			sb.append("Content-Disposition: form-data; name=\""
					+ param.getKey() + "\"" + lineEnd);
			sb.append(lineEnd);
			sb.append(param.getValue() + lineEnd);
		}
		try {
			output.writeBytes(sb.toString());// 发送表单字段数据
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 直接通过 HTTP 协议提交数据到服务器，实现表单提交功能。
	 * 
	 * @param actionUrl
	 *            上传路径
	 * @param params
	 *            请求参数key为参数名，value为参数值
	 * @param files
	 *            上传文件信息
	 * @return 返回请求结果
	 */
	public static String post(String actionUrl,
			Set<Map.Entry<Object, Object>> params, Image[] files) {
		HttpURLConnection conn = null;
		DataOutputStream output = null;
		BufferedReader input = null;
		try {
			URL url = new URL(actionUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(120000);
			conn.setDoInput(true); // 允许输入
			conn.setDoOutput(true); // 允许输出
			conn.setUseCaches(false); // 不使用Cache
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Content-Type", multipart_form_data
					+ "; boundary=" + boundary);

			conn.connect();
			output = new DataOutputStream(conn.getOutputStream());

			addImageContent(files, output); // 添加图片内容

			addFormField(params, output); // 添加表单字段内容

			output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);// 数据结束标志
			output.flush();

			int code = conn.getResponseCode();
			if (code != 200) {
				throw new RuntimeException("请求‘" + actionUrl + "’失败！");
			}

			input = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String oneLine;
			while ((oneLine = input.readLine()) != null) {
				response.append(oneLine + lineEnd);
			}

			return response.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			// 统一释放资源
			try {
				if (output != null) {
					output.close();
				}
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static class Image {

		String formName, fileName;

		byte[] data;

		Image(String formName, String fileName, Bitmap src) throws IOException {
			this.fileName = fileName;
			this.formName = formName;
			// bitmap = src;
			if (src == null) {
				throw new NullPointerException();
			}

			// 将bitmap编码为png
			File f = new File(PATH_TEMP_FOLDER + "/" + fileName + ".png");
			f.mkdirs();
			if (f.exists()) {
				if (!f.delete()) {
					throw new IOException();
				}
			}

			PictureManager.saveBitmap(src, f.getAbsolutePath());

			data = toByteArray(f.getAbsolutePath());
		}

		String getFormName() {
			return formName;
		}

		String getFileName() {
			return fileName;
		}

		String getContentType() {
			return "image/png";
		}

		byte[] getData() {
			return data;
		}

		private static byte[] toByteArray(String filename) throws IOException {

			FileChannel fc = null;
			RandomAccessFile ra = null;
			try {
				ra = new RandomAccessFile(filename, "r");
				fc = ra.getChannel();
				MappedByteBuffer byteBuffer = fc.map(MapMode.READ_ONLY, 0,
						fc.size()).load();
				System.out.println(byteBuffer.isLoaded());
				byte[] result = new byte[(int) fc.size()];
				if (byteBuffer.remaining() > 0) {
					// System.out.println("remain");
					byteBuffer.get(result, 0, byteBuffer.remaining());
				}
				return result;
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			} finally {
				try {
					fc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					ra.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
