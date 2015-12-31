package com.dingfch.okhttplib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

/**
 * 网络请求工具类
 * @author dingfangchao
 */
@SuppressLint("NewApi")
public class HttpUtils {
	private static HttpUtils mInstance;
	private static Headers mHeaders = null;
	private static boolean mNeedDecodeResult = false;
	private static final String defaultCode = "requestCode_default";
	private static final OkHttpClient mOkHttpClient = new OkHttpClient();
	private static final Handler mDelivery = new Handler(Looper.getMainLooper());
	private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
	private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");
	static {
		mOkHttpClient.setConnectTimeout(10, TimeUnit.SECONDS);
		mOkHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
		mOkHttpClient.setWriteTimeout(30, TimeUnit.SECONDS);
		HostnameVerifier hostnameVerifier = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
	      };
	      mOkHttpClient.setHostnameVerifier(hostnameVerifier);
	}
	//
//	private static boolean mCacheData = false;
	
	/**
	 * 单向认证 调用 {@link #setCertificates(InputStream...)} 
	 * 双向认证 调用{@link #setCertificates(InputStream[], InputStream, String)}}
	 */
	public static HttpUtils getInstance(){
		if (mInstance == null){
			synchronized (HttpUtils.class){
				if (mInstance == null){
					mInstance = new HttpUtils();
				}
			}
		}
		return mInstance;
	}

	public static void setCacheData(Context context) {
		if (!CacheUtils.isInited()) {
			CacheUtils.initCache(context, "NetCache");
		}
	}

	public static void clearCacheData() {
		CacheUtils.clearCacheDate();
	}
	
	public static void setDecodeResult(boolean decodeResult) {
		mNeedDecodeResult = decodeResult;
	}
	
	public static void setRequestHeader(Map<String, String> headers, HeaderCallBack callBack){
		if(headers != null){
			mHeaders = Headers.of(headers);
			if(callBack != null){
				if(mHeaders != null){
					callBack.onHeaderSuccess();
				}else{
					callBack.onHeaderError();
				}
			}
		}
	}
	/**
	 * 
	 * @param url 请求url
	 * @return
	 */
	private static Request requestFactory(String url){
		return requestFactory(url, null, false);
	}
	
	/**
	 * Request Factory
	 * @param url 请求url
	 * @param params 参数
	 * @param post true：post 方式； false ：get方式
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Request requestFactory(String url, JSONObject params, boolean post){
		Request request = null;
		RequestBody formBody = null;
		if(post){
			FormEncodingBuilder buider = new FormEncodingBuilder();
			if(params != null && params.keys() != null){
				for (Iterator<String> iterator = params.keys(); iterator.hasNext();) {
					String key = iterator.next();
					buider.add(key, params.optString(key));
				}
				formBody = buider.build();
			}
		} else {
			if(params != null && params.length() > 0){
				if (url.endsWith("?")) {
					url = url + json2String(params);
				} else {
					url = url + "?" + json2String(params);
				}
			}
			
		}
		
		if(formBody == null){
			if(mHeaders != null){
				request = new Request.Builder().headers(mHeaders).url(url).build();
			}else{
				request = new Request.Builder().url(url).build();
			}
		} else{
			if(post){
				if(mHeaders != null){
					request = new Request.Builder().headers(mHeaders).url(url).post(formBody).build();
				}else{
					request = new Request.Builder().url(url).post(formBody).build();
				}
			}
		}
		return request;
	}

	/**
	 * GET 异步
	 * @param url 请求url
	 * @param callback 回调
	 */
	public static void requestServer(final String url, final NetCallBack callback) {
		requestServer(defaultCode, url, null, callback, false);
	}
	
	public static void requestServer(final String url, final NetCallBack callback, boolean bNeedCacheData) {
		requestServer(defaultCode, url, null, callback, bNeedCacheData);
	}

	/**
	 * GET 异步
	 * @param url 请求url
	 * @param params 请求参数
	 * @param callback 回调
	 */
	public static void requestServer(final String url, JSONObject params, final NetCallBack callback) {
		requestServer(defaultCode, url, params, callback, false);
	}
	
	public static void requestServer(final String url, JSONObject params, final NetCallBack callback, boolean bNeedCacheData) {
		requestServer(defaultCode, url, params, callback, bNeedCacheData);
	}

	/**
	 * POST 异步
	 * @param url 请求url
	 * @param params 请求参数
	 * @param callback 回调
	 */
	public static void requestServerByPost(final String url, JSONObject params, final NetCallBack callback) {
		requestServerByPost(defaultCode, url, params, callback, false);
	}
	
	public static void requestServerByPost(final String url, JSONObject params, final NetCallBack callback, boolean bNeedCacheData) {
		requestServerByPost(defaultCode, url, params, callback, bNeedCacheData);
	}
//
//	/**
//	 * GET 异步
//	 * @param requestCode 请求码 ，为在同一类中区分多个请求
//	 * @param url 请求 url
//	 * @param callback 回调
//	 */
//	public static void requestServer(final String requestCode, final String url, final NetCallBack callback, boolean bNeedCacheData) {
//		Request request = requestFactory(url);
//		startRequest(callback, requestCode);
//		mOkHttpClient.newCall(request).enqueue(new Callback() {
//			@Override
//			public void onResponse(Response result) throws IOException {
//				afterRequest(callback, requestCode);
//				String resultData = "";
//				if(mNeedDecodeResult){
//					resultData = URLDecoder.decode(result.body().string(), "UTF-8");
//				}else{
//					resultData = result.body().string();
//				}
//				final String temp = resultData;
//				if(callback != null){
//					mDelivery.post(new Runnable() {
//						@Override
//						public void run() {
//							callback.onSuccess(requestCode.equals(defaultCode)? "" : requestCode, temp);
//							Log.e("HttpUtils", url);
//							Log.e("HttpUtils", temp);
//						}
//					});
//				}
//				
//				if (mCacheData) {
//					CacheUtils.cacheData(url, temp);
//				}
//			}
//
//			@Override
//			public void onFailure(Request arg0, final IOException error) {
//				final String cacheData = CacheUtils.getCacheData(url);
//				afterRequest(callback, requestCode);
//				
//				if (mCacheData && !TextUtils.isEmpty(cacheData)) {
//					if(callback != null){
//						mDelivery.post(new Runnable() {
//							@Override
//							public void run() {
//								callback.onSuccess(requestCode.equals(defaultCode)? "" : requestCode, cacheData);
//							}
//						});
//					}
//				} else {
//					if(callback != null){
//						mDelivery.post(new Runnable() {
//							@Override
//							public void run() {
//								callback.onError(error.toString());
//							}
//						});
//					}
//				}
//			}
//		});
//	}

	/**
	 * GET 异步
	 * @param requestCode 请求码
	 * @param url 请求url
	 * @param params 参数
	 * @param callback 回调
	 */
	public static void requestServer(final String requestCode, final String url, JSONObject params, final NetCallBack callback, final boolean bNeedCacheData) {
		Request request = requestFactory(url, params, false);
		startRequest(callback, requestCode);
		mOkHttpClient.newCall(request).enqueue(new Callback() {

			@Override
			public void onResponse(Response result) throws IOException {
				String resultData = "";
				afterRequest(callback, requestCode);
				if(mNeedDecodeResult){
					resultData = URLDecoder.decode(result.body().string(), "UTF-8");
				}else{
					resultData = result.body().string();
				}
				final String temp = resultData;
				if(callback != null){
					mDelivery.post(new Runnable() {
						@Override
						public void run() {
							callback.onSuccess(requestCode.equals(defaultCode)? "" : requestCode, temp);
						}
					});
				}
				
				if (bNeedCacheData) {
					CacheUtils.cacheData(url, temp);
				}
			}

			@Override
			public void onFailure(Request arg0, final IOException error) {
				final String cacheData = CacheUtils.getCacheData(url);
				afterRequest(callback, requestCode);
				
				if (bNeedCacheData && !TextUtils.isEmpty(cacheData)) {
					if(callback != null){
						mDelivery.post(new Runnable() {
							@Override
							public void run() {
								callback.onSuccess(requestCode.equals(defaultCode)? "" : requestCode, cacheData);
							}
						});
					}
				} else {
					if(callback != null){
						mDelivery.post(new Runnable() {
							@Override
							public void run() {
								callback.onError(error.toString());
							}
						});
					}
				}
			}
		});
	}

	/**
	 * POST 异步
	 * @param requestCode 请求码
	 * @param url 请求url
	 * @param params 参数
	 * @param callback 回调
	 */
	public static void requestServerByPost(final String requestCode, final String url, JSONObject params, final NetCallBack callback, final boolean bNeedCacheData) {
		Request request = requestFactory(url, params, true);
		startRequest(callback, requestCode);
		mOkHttpClient.newCall(request).enqueue(new Callback() {

			@Override
			public void onResponse(Response result) throws IOException {
				afterRequest(callback, requestCode);
				String resultData = "";
				if(mNeedDecodeResult){
					resultData = URLDecoder.decode(result.body().string(), "UTF-8");
				}else{
					resultData = result.body().string();
				}
				final String temp = resultData;
				if(callback != null){
					mDelivery.post(new Runnable() {
						@Override
						public void run() {
							callback.onSuccess(requestCode.equals(defaultCode)? "" : requestCode, temp);
						}
					});
				}
				
				if (bNeedCacheData) {
					CacheUtils.cacheData(url, temp);
				}
			}

			@Override
			public void onFailure(Request arg0, final IOException error) {
				afterRequest(callback, requestCode);

				final String cacheData = CacheUtils.getCacheData(url);
				if (bNeedCacheData && !TextUtils.isEmpty(cacheData)) {
					if(callback != null){
						mDelivery.post(new Runnable() {
							@Override
							public void run() {
								callback.onSuccess(requestCode.equals(defaultCode)? "" : requestCode, cacheData);
							}
						});
					}
				} else {
					if(callback != null){
						mDelivery.post(new Runnable() {
							@Override
							public void run() {
								callback.onError(error.toString());
							}
						});
					}
				}
			}
		});
	}
	
	//--synchronization request
	/**
	 * 同步GET
	 * @param url 请求的 url
	 * @return
	 */
	public static String synRequestServer(final String url){
		return synRequestServer(url, null);
	}
	
	/**
	 * 同步GET
	 * @param url 请求的url
	 * @param params 参数
	 * @return
	 */
	public static String synRequestServer(final String url, JSONObject params){
		Request request = requestFactory(url, params, false);
		try {
			Response response = mOkHttpClient.newCall(request).execute();
			return response.body().string();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 加载图片 返回 InputStream
	 * @param url
	 * @return
	 */
	public static InputStream loadImageStream(String url){
		Request.Builder builder = new Request.Builder().url(url);
		try {
			Response response = mOkHttpClient.newCall(builder.build()).execute();
			int responseCode = response.code();
			if (responseCode >= 300) {
				try {
					response.body().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				return response.body().byteStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
		}
	    return null;
	}
	
	/**
	 * 同步POST
	 * @param url 请求的url
	 * @param params 参数
	 * @return
	 */
	public static String synRequestServerByPost(final String url, JSONObject params){
		Request request = requestFactory(url, params, true);
		try {
			Response response = mOkHttpClient.newCall(request).execute();
			return response.body().string();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static void startRequest(final NetCallBack callback, final String requestCode){
		if(callback != null){
			mDelivery.post(new Runnable() {
				@Override
				public void run() {
					callback.onBefore(requestCode.equals(defaultCode)? "" : requestCode);
				}
			});
		}
	}
	
	private static void afterRequest(final NetCallBack callback, final String requestCode){
		if(callback != null){
			mDelivery.post(new Runnable() {
				@Override
				public void run() {
					callback.onAfter(requestCode.equals(defaultCode)? "" : requestCode);
				}
			});
		}
	}
	
	//upload image
	@SuppressLint("DefaultLocale")
	public static void uploadImage(String url, String bodyName, ArrayList<String> fileNames, final NetCallBack callback){
		MultipartBuilder builder = new MultipartBuilder().type(MultipartBuilder.FORM);
		String name = "";
		for (int i = 0; i < fileNames.size(); ++i) {
			name = fileNames.get(i);
			if(name.toLowerCase().endsWith("png")){
				builder.addFormDataPart(bodyName + i, null, RequestBody.create(MEDIA_TYPE_PNG, new File(name)));
			} else {
				builder.addFormDataPart(bodyName + i, null, RequestBody.create(MEDIA_TYPE_JPG, new File(name)));
			}
		}
		RequestBody requestBody = builder.build();
		Request request = null;
		if(mHeaders != null){
			request = new Request.Builder().headers(mHeaders).url(url).post(requestBody).build();
		}else{
			request = new Request.Builder().url(url).post(requestBody).build();
		}
		startRequest(callback, "");
		mOkHttpClient.newCall(request).enqueue(new Callback() {
			
			@Override
			public void onResponse(Response result) throws IOException {
				String resultData = "";
				try {
					if(mNeedDecodeResult){
						resultData = URLDecoder.decode(result.body().string(), "UTF-8");
					}else{
						resultData = result.body().string();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				final String temp = resultData;
				if(callback != null){
					mDelivery.post(new Runnable() {
						@Override
						public void run() {
							callback.onSuccess("", temp);
						}
					});
				}
				afterRequest(callback, "");
			}
			
			@Override
			public void onFailure(Request arg0, final IOException error) {
				if(callback != null){
					mDelivery.post(new Runnable() {
						@Override
						public void run() {
							callback.onError(error.toString());
						}
					});
				}
				afterRequest(callback, "");
			}
		});
	}

	@SuppressWarnings("unchecked")
	private static String json2String(JSONObject params) {
		String content = "";
		for (Iterator<String> iterator = params.keys(); iterator.hasNext();) {
			String key = iterator.next();
			if (!TextUtils.isEmpty(content)) {
				content += "&";
			}
			try {
				content += key + "=" + URLEncoder.encode(params.opt(key).toString(),"UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return content;
	}
	
	// add support for https
	
	/**
	 * 信任所有证书签名
	 */
	public void setCertificates() {
		setCertificates(null, null, null);
	}
	
	/**
	 * 单项认证
	 * @param certificates 服务端证书
	 */
	public void setCertificates(InputStream... certificates) {
		setCertificates(certificates, null, null);
	}
	
	/**
	 * 双向认证
	 * @param certificates 服务端证书
	 * @param bksFile 客户端签名
	 * @param password 签名密码
	 */
	@SuppressLint("TrulyRandom")
	public void setCertificates(InputStream[] certificates, InputStream bksFile, String password) {
		try {
			TrustManager[] trustManagers = prepareTrustManager(certificates);
			KeyManager[] keyManagers = prepareKeyManager(bksFile, password);
			SSLContext sslContext = SSLContext.getInstance("TLS");

			sslContext.init(keyManagers, new TrustManager[] { new CustomeTrustManager(chooseTrustManager(trustManagers)) }, new SecureRandom());
			mOkHttpClient.setSslSocketFactory(sslContext.getSocketFactory());
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
	}

	private TrustManager[] prepareTrustManager(InputStream... certificates) {
		if (certificates == null || certificates.length <= 0)
			return null;
		try {

			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null);
			int index = 0;
			for (InputStream certificate : certificates) {
				String certificateAlias = Integer.toString(index++);
				keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
				try {
					if (certificate != null){
						certificate.close();
					}
				} catch (IOException e){
				}
			}
			TrustManagerFactory trustManagerFactory = null;

			trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(keyStore);

			TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

			return trustManagers;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	private KeyManager[] prepareKeyManager(InputStream bksFile, String password) {
		try {
			if (bksFile == null || password == null)
				return null;

			KeyStore clientKeyStore = KeyStore.getInstance("BKS");
			clientKeyStore.load(bksFile, password.toCharArray());
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(clientKeyStore, password.toCharArray());
			return keyManagerFactory.getKeyManagers();

		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
		for (TrustManager trustManager : trustManagers) {
			if (trustManager instanceof X509TrustManager) {
				return (X509TrustManager) trustManager;
			}
		}
		return null;
	}

	private class CustomeTrustManager implements X509TrustManager {
		private X509TrustManager defaultTrustManager;
		private X509TrustManager localTrustManager;

		public CustomeTrustManager(X509TrustManager localTrustManager)
				throws NoSuchAlgorithmException, KeyStoreException {
			TrustManagerFactory var4 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			var4.init((KeyStore) null);
			defaultTrustManager = chooseTrustManager(var4.getTrustManagers());
			this.localTrustManager = localTrustManager;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			try {
				defaultTrustManager.checkServerTrusted(chain, authType);
			} catch (CertificateException ce) {
				localTrustManager.checkServerTrusted(chain, authType);
			}
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}

	private HttpUtils() { /* do not new me */
	}

	
	
}
