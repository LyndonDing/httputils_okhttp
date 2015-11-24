/*
 * Copyright (C) dingfangch@gmail.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	/**
	 * 请求成功
	 * @param requestCode 操作码
	 * @param result 请求结果
     */
	abstract public void onSuccess(String requestCode, String result);

	/**
	 * 请求失败
	 * @param errMsg 异常信息
     */
	abstract public void onError(String errMsg);
}