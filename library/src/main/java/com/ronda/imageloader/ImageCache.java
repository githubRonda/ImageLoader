package com.ronda.imageloader;

import android.graphics.Bitmap;

/**
 * Created by Ronda on 2018/1/8.
 */

public interface ImageCache {
    public Bitmap get(String url);
    public void put(String url, Bitmap bitmap);
}
