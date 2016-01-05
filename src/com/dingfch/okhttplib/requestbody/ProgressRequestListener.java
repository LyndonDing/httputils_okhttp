package com.dingfch.okhttplib.requestbody;
/**
 * 请求体进度回调接口，文件上传进度
 */
public interface ProgressRequestListener {
	void onRequestProgress(long bytesWritten, long contentLength, boolean done);
}
