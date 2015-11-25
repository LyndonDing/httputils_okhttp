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
