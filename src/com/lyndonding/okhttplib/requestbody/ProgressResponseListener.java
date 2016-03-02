package com.lyndonding.okhttplib.requestbody;

/**
 * 响应体进度回调，文件下载进度
 */
public interface ProgressResponseListener {
	void onResponseProgress(long bytesRead, long contentLength, boolean done);
}
