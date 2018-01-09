package com.ronda.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Ronda on 2018/1/8.
 * <p>
 * 单一职责原则: 把图片加载和图片缓存分开
 * 开闭原则: 对于拓展开放,对于修改封闭. 新增磁盘缓存
 * 里氏替换原则
 * 依赖倒置原则
 * 接口隔离原则: CloseUtils
 * 迪米特原则
 * <p>
 * 1. 缓存策略: 优先使用内存缓存, 其实是磁盘缓存, 最后是网络获取
 * 2. 其实可以模仿Picasso或Glide的链式调用方式来加载图片(默认图, 出错图等)
 */

public class ImageLoader {
    // 图片缓存
    private ImageCache mImageCache = new DoubleCache(); // ImageLoader 依赖抽象,并且有一个默认实现, 默认使用双缓存

    // 线程池, 线程数量为cpu可提供的线程数
    ExecutorService mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public ImageLoader() {

    }

    public void setImageCache(ImageCache imageCache) {
        this.mImageCache = imageCache;
    }

    /**
     * 加载图片
     */
    public void displayImage(ImageView imageView, String url, Drawable errorDrawable) {

        Bitmap bitmap = mImageCache.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }

        // 若没有缓存, 则提交到线程池中下载图片
        submitLoadRequest(imageView, url, errorDrawable);
    }

    public void displayImage(ImageView imageView, String url, int defDrawableRes, Integer errorDrawableRes) {
        // 设置一个默认图片
        imageView.setImageResource(defDrawableRes);

        Drawable drawable = null;
        if (errorDrawableRes != null){
            drawable = imageView.getResources().getDrawable(errorDrawableRes);
        }
        displayImage(imageView, url, drawable);
    }

    public void displayImage(ImageView imageView, String url) {
        displayImage(imageView, url, null);
    }

    public void displayImage(ImageView imageView, String url, int defDrawableRes) {
        displayImage(imageView, url, defDrawableRes, null);
    }


    private void submitLoadRequest(final ImageView imageView, final String url, final Drawable errorDrawable) {
        /**
         * 给imageView设置图片
         * 注意: 由于listView的ItemView重用机制导致 和 网络加载的不稳定性和延迟性. 在滑动的时候, 每个itemView都发起了获取图片的请求, 但是
         * 请求的返回结果是有延迟的,而且返回的顺序也是随机的(取决于发起请求那一刻的网速), 所以可能会出现新展示的ItemView加载的图片是复用之前的itemView所请求的图片
         * 总的来说, 就是由于复用机制, 导致同一个itemView 多次发起了获取不同图片的请求, 这样当结果返回进行加载显示的时候,可能会出现加载错误图片的现象, 应该显示最后一次请求的图片
         * 所以需要在此处给ImageView设置tag,进行校验, 判断是否是正确的图片
         */
        imageView.setTag(url);  //setTag()方法必须要在主线程中调用
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {

                Bitmap bitmap = downloadImage(url);
                if (bitmap != null) {
                    mImageCache.put(url, bitmap);
                }
                if (imageView.getTag().equals(url)) {// 判断该请求是否是ImageView最后一次发起的
                    if (bitmap == null) {
                        if (errorDrawable != null) {
                            imageView.setImageDrawable(errorDrawable); // 设置获取失败的图片
                        }
                    } else {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        });
    }


    public Bitmap downloadImage(String imageUrl) {
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(imageUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);// 连接超时
            conn.setReadTimeout(5000);// 读取超时. 连接成功后,响应数据的时间
            conn.connect(); // 进行连接

            int responseCode = conn.getResponseCode();// 获取响应码
            if (responseCode == 200) { // 成功
                InputStream inputStream = conn.getInputStream();

                // 根据输入流生成bitmap对象
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

        }
        return bitmap;
    }


    //=======================================================================================
    // 这里提供一种使用AsyncTask来下载图片的异步请求方式, 不同于上面的使用线程池的方式, 这种方式可以实现
    // 具体进度的更新.
    //=======================================================================================
    public void display(ImageView imageView, String url) {
        // AsyncTask 异步封装的工具, 可以实现异步请求及主界面更新(对线程池+handler的封装)

        new BitmapTask(imageView, url).execute(); // 启动AsyncTask
    }


    /**
     * 三个泛型意义:
     * 第一个泛型:doInBackground里的参数类型, 而且也是AsyncTask.execute()中不定参数的类型, 所以execute()方法就是把形参传递给了doInBackground()
     * 第二个泛型:onProgressUpdate里的参数类型, 而且也是AsyncTask.publishProgress()中不定参数的类型,所以publishProgress()方法就是把形参传递给了onProgressUpdate(), 而且可能传递多个值,eg: 总大小, 当前进度, 百分比等, 所以是一个不定形参
     * 第三个泛型:onPostExecute里的参数类型及doInBackground的返回类型
     * <p>
     * 总结: 这三个参数就相当于事情的开始, 经过, 结果 这三个阶段.
     * 一开始, 我们需要把参数传递给子线程进行耗时操作, 操作过程中可以公布一下进度情况, 操作完成后子线程需要把结果返回并且还要结果传递给主线程中的回调方法便于更新UI
     * <p>
     * 其实把这三个参数类型都设为Void, 直接使用成员变量传递这些参数也可以.
     */
    class BitmapTask extends AsyncTask<Void, Integer, Bitmap> {

        private ImageView mImageView;
        private String mUrl;

        public BitmapTask(ImageView imageView, String url) {
            this.mImageView = imageView;
            this.mUrl = url;

            mImageView.setTag(mUrl); //setTag()方法必须要在主线程中调用
        }


        // 1.预加载, 运行在主线程
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // 2.正在加载, 运行在子线程(核心方法), 可以直接异步请求
        @Override
        protected Bitmap doInBackground(Void... params) {

            // 开始下载图片
            Bitmap bitmap = downloadImage(mUrl);

            //publishProgress();

            return bitmap;
        }

        // 3.更新进度的方法, 运行在主线程. 调用publishProgress()会回调此方法
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        // 4.加载结束, 运行在主线程(核心方法), 可以直接更新UI
        @Override
        protected void onPostExecute(Bitmap result) {

            if (mImageView.getTag().equals(mUrl)) {// 判断tag是否和初始设置的一样
                mImageView.setImageBitmap(result);

                // 写入缓存
                mImageCache.put(mUrl, result);
            }
        }
    }
}
