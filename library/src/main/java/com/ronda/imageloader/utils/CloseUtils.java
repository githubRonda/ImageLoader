package com.ronda.imageloader.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Ronda on 2018/1/8.
 */

public class CloseUtils {
    private CloseUtils(){}

    /**
     * 关闭Closeable对象.
     * 原理:依赖Closeable抽象而不是具体实现,并且建立子啊最小化依赖的原则的基础上,即接口隔离原则
     * @param closeable
     */
    public static void close(Closeable closeable){
        if (null != closeable){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
