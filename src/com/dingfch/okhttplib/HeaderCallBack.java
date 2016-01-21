package com.dingfch.okhttplib;

public interface HeaderCallBack{
	abstract public void onHeaderSuccess();
	abstract public void onHeaderError(String errInfo);
}