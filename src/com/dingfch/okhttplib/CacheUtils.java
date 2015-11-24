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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.text.TextUtils;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * cache data utils
 * Using DiskLruCache
 * @author dingfangchao
 * @time 2015-8-11
 */
@SuppressLint("NewApi")
public class CacheUtils {
	//
    private static DiskLruCache mDiskLruCache;
    //
    private static long mMaxCacheSize = 10*1024*1024;
    //
    private static String mCacheFileName = "defaultCache";
    //
    private static File mCacheDir;
    //
    private static boolean mInited = false;;
    
	public static void initCache(Context context, String name){
    	//init cache data
    	try {
    		mCacheDir = getDiskCacheDir(context, TextUtils.isEmpty(name)? mCacheFileName : name);  
    	    if (!mCacheDir.exists()) {  
    	    	mCacheDir.mkdirs();  
    	    }
    		mDiskLruCache = DiskLruCache.open(mCacheDir, getAppVersionCode(context), 1, mMaxCacheSize);
    		mInited = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public static boolean isInited(){
		return mInited;
	}
	
	public static File getCacheFile(){
		return mCacheDir;
	}
	
	public static void clearCacheDate(){
		try {
			mDiskLruCache.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void cacheData(final String key, final String data){
		CacheThreadPool.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if(mDiskLruCache != null){
					synchronized (mDiskLruCache) {
						String tempKey = hashKeyForDisk(key);
						try {
							DiskLruCache.Editor editor = mDiskLruCache.edit(tempKey);
							if (editor == null){
								return;
				            }
							ObjectOutputStream out = new ObjectOutputStream(editor.newOutputStream(0));
				            out.writeObject(data);
				            out.close();
							//提交数据
							editor.commit();
							//同步日志
							mDiskLruCache.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		
	}
	
	public static String getCacheData(String key) {
		if (mDiskLruCache != null) {
			synchronized (mDiskLruCache) {
				DiskLruCache.Snapshot snapshot;
				try {
					String tempKey = hashKeyForDisk(key);
					snapshot = mDiskLruCache.get(tempKey);
					if (snapshot == null){
						return null;
		            }
					ObjectInputStream in = new ObjectInputStream(snapshot.getInputStream(0));
					return (String) in.readObject();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException ex) {
					ex.printStackTrace();
				}
			}
		}
		return null;
	}
	
	private static String hashKeyForDisk(String key) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}
	
	
	/**
     * @param context
     * @param uniqueName customer file name of cache
     * @return
     */
	public static File getDiskCacheDir(Context context, String uniqueName) {
    	String cachePath;
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
    			|| !Environment.isExternalStorageRemovable()) {
    		cachePath = context.getExternalCacheDir().getPath();
    	} else {
    		cachePath = context.getCacheDir().getPath();
    	}
    	return new File(cachePath + File.separator + uniqueName);
    }
    
    public static int getAppVersionCode(Context context) {
		PackageManager pm = context.getPackageManager();
		PackageInfo info = null;
		try {
			info = pm.getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return info.versionCode;
	}
}
