package com.dingfch.okhttplib;

public interface NetCallBack{
	/**
	 * 开始请求
	 */
	public void onBefore(String requestCode);
	/**
	 * 请求结束
	 */
	public void onAfter(String requestCode);
	
	abstract public void onSuccess(String requestCode, String result);
	abstract public void onError(String errMsg);
}