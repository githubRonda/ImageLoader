package com.ronda.imageloader;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by Ronda on 2018/1/8.
 *
 * Android 中内存缓存, Google建议使用LruCache, 而非使用软引用或弱引用 (SoftReference or WeakReference).
 * 因为从 Android 2.3 (API Level 9)开始，垃圾回收器会更倾向于回收持有软引用或弱引用的对象，这让软引用和弱引用变得不再可靠。
 */

public class MemoryCache implements ImageCache{

    // 图片Lru缓存
    LruCache<String, Bitmap> mMemoryCache;

    public MemoryCache() {
        initImageCache();
    }

    private void initImageCache() {
        // 计算可使用的最大内存
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024); // 单位Kb

        // 取四分之一的可用内存作为缓存
        final int cacheSize = maxMemory / 4;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024; // 计算bitmap大小, 单位Kb, 同构造器形参一致
            }
        };
    }

    @Override
    public void put(String url, Bitmap bitmap) {

        mMemoryCache.put(url, bitmap);
    }

    @Override
    public Bitmap get(String url) {

        return mMemoryCache.get(url);
    }
}
