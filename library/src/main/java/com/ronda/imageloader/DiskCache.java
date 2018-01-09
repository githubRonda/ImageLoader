package com.ronda.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.ronda.imageloader.utils.CloseUtils;
import com.ronda.imageloader.utils.MD5Encoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by Ronda on 2018/1/8.
 * 磁盘缓存
 * 注意: 若url直接作为文件名的话, 是有特殊符号的, 所以进行MD5加密后在作为文件名. 内存缓存是不存在这种情况的.
 */

public class DiskCache implements ImageCache {

    private static final String LOCAL_CACHE_PATH = Environment.getExternalStorageDirectory() + "/image_cache";

    // 从缓存中获取图片
    @Override
    public Bitmap get(String url) {

        Bitmap bitmap = BitmapFactory.decodeFile(LOCAL_CACHE_PATH + File.separator + MD5Encoder.encode(url));
        return bitmap;
    }

    //将图片缓存到内存中
    @Override
    public void put(String url, Bitmap bitmap) {

        Log.d("Liu", "disk cache dir: " + LOCAL_CACHE_PATH);

        File dir = new File(LOCAL_CACHE_PATH);
        // 创建文件夹
        if (!dir.exists() || !dir.isDirectory()) {//如果文件夹不存在, 或者不是一个目录
            dir.mkdirs(); //创建多级目录
        }

        FileOutputStream fos = null;
        try {
            //因为文件的命名对于url中的某些字符有限制, 所以采用md5对其进行加密. 只有文件有这种要求
            String fileName = MD5Encoder.encode(url);

            //把图片压缩,然后写入到一个输出流中. 保存至本地
            fos = new FileOutputStream(new File(dir, fileName));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
           CloseUtils.close(fos);
        }
    }
}
