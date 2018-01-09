package com.ronda.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.ronda.imageloader.DoubleCache;
import com.ronda.imageloader.ImageLoader;


public class MainActivity extends AppCompatActivity {

    private ImageView iv_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv_show = (ImageView) findViewById(R.id.iv_show);


        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageLoader imageLoader = new ImageLoader();
                imageLoader.setImageCache(new DoubleCache());// 默认就是DoubleCache(内存缓存+磁盘缓存)
                imageLoader.displayImage(iv_show, "https://www.baidu.com/img/bd_logo1.png");
            }
        });


    }
}
