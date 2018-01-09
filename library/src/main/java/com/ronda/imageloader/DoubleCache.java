package com.ronda.imageloader;

import android.graphics.Bitmap;

/**
 * Created by Ronda on 2018/1/8.
 *
 * 双缓存类: 内存缓存和磁盘缓存
 */

public class DoubleCache implements ImageCache {

    private MemoryCache mMemoryCache = new MemoryCache();
    private DiskCache mDiskCache = new DiskCache();

    // 先从内存缓存中获取, 如果没有,则再从磁盘缓存中获取
    @Override
    public Bitmap get(String url) {
        // 优先内存加载
        Bitmap bitmap = mMemoryCache.get(url);
        if (bitmap != null){
            return bitmap;
        }

        // 其次本地加载
        bitmap = mDiskCache.get(url);
        if (bitmap != null){
            // 写入内存缓存
            mMemoryCache.put(url, bitmap);
        }
        return bitmap;
    }

    // 将图片缓存到内存和磁盘中
    @Override
    public void put(String url, Bitmap bitmap) {
        mMemoryCache.put(url, bitmap);
        mDiskCache.put(url, bitmap);
    }
}
