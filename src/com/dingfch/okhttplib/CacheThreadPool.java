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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CacheThreadPool {
	public static ExecutorService mThreadPool;
	private static int mThreadSize = 2;
	public static ExecutorService getThreadPool() {
		if (mThreadPool == null) {
			mThreadPool = Executors.newFixedThreadPool(mThreadSize);
		}
		return mThreadPool;
	}

	public static void shutdown() {
		if (mThreadPool != null) {
			synchronized (mThreadPool) {
				if (!mThreadPool.isShutdown()) {
					mThreadPool.shutdownNow();
					mThreadPool = null;
				}
			}
		}
	}
}
