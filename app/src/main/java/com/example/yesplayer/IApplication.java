package com.example.yesplayer;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by steven on 2021/3/8.
 */

public class IApplication extends Application {

    private static Application application;
    private static ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        executorService = Executors.newSingleThreadExecutor();
    }

    public static Context _getContext(){
        return application.getApplicationContext();
    }

    public static ExecutorService getExecutor(){
        return executorService;
    }
}
