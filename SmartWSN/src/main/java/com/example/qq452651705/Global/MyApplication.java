package com.example.qq452651705.Global;

import android.app.Application;

/**
 * Created by B on 2018/2/18.
 */
/**
 *  全局上下文
 */
public class MyApplication extends Application {
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MyApplication getInstance(){
        // 因为我们程序运行后，Application是首先初始化的，如果在这里不用判断instance是否为空
        return instance;
    }
}
