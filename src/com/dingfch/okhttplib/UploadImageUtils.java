package com.dingfch.okhttplib;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONObject;

import com.dingfch.okhttplib.requestbody.ProgressRequestListener;
import com.dingfch.okhttplib.requestbody.UploadRequestBody;

import android.annotation.SuppressLint;
import android.os.Handler;

@SuppressLint("DefaultLocale")
public class UploadImageUtils {
	private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
	private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");
	private static final MediaType MEDIA_TYPE_GIF = MediaType.parse("image/gif");
	// upload image
	@SuppressWarnings("unchecked")
	public void uploadImage(OkHttpClient client, 
							Headers requestHander, 
							final Handler handler, 
							String url, 
							JSONObject bodyNamesToFilePath, 
							ProgressRequestListener listener,
							final NetCallBack callback) {
		MultipartBody.Builder builder = new MultipartBody.Builder() .setType(MultipartBody.FORM);
		try {
			if (bodyNamesToFilePath != null && bodyNamesToFilePath.keys() != null) {
				for (Iterator<String> iterator = bodyNamesToFilePath.keys(); iterator.hasNext();) {
					String bodyName = iterator.next();
					String filePath = bodyNamesToFilePath.optString(bodyName);
					File imageFile = new File(filePath);
					if (filePath.toLowerCase().endsWith("png")) {
						builder.addFormDataPart(bodyName, imageFile.getName(), RequestBody.create(MEDIA_TYPE_PNG, imageFile));
					} else if (filePath.toLowerCase().endsWith("jpg") || filePath.toLowerCase().endsWith("jpeg")){
						builder.addFormDataPart(bodyName, imageFile.getName(), RequestBody.create(MEDIA_TYPE_JPG, imageFile));
					} else if (filePath.toLowerCase().endsWith("gif")){
						builder.addFormDataPart(bodyName, imageFile.getName(), RequestBody.create(MEDIA_TYPE_GIF, imageFile));
					}
				}
			}
		} catch (Exception e) {
			if (callback != null) {
				callback.onError(e.toString());
			}
			return;
		}

		RequestBody requestBody = builder.build();
		Request request = null;
		if (requestHander != null) {
			request = new Request.Builder()
								.headers(requestHander)
								.url(url)
								.post(listener == null ? requestBody : new UploadRequestBody(requestBody, listener))
								.build();
		} else {
			request = new Request.Builder()
								.url(url)
								.post(listener == null ? requestBody : new UploadRequestBody(requestBody, listener))
								.build();
		}
		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onResponse(Response result) throws IOException {
				if(callback != null){
					handler.post(new Runnable() {
						@Override
						public void run() {
							callback.onAfter("");
						}
					});
				}
				String resultData = "";
				resultData = result.body().string();
				final String temp = resultData;
				if (callback != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							callback.onSuccess("", temp);
						}
					});
				}
			}

			@Override
			public void onFailure(Request arg0, final IOException error) {
				if(callback != null){
					handler.post(new Runnable() {
						@Override
						public void run() {
							callback.onAfter("");
						}
					});
				}
				if (callback != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							callback.onError(error.toString());
						}
					});
				}
			}
		});
	}
	
}
