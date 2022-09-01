package com.frankhon.fantasymusic.application;

import static com.frankhon.fantasymusic.utils.Notification.createNotificationChannel;

import android.app.Application;
import android.content.Context;

import com.frankhon.fantasymusic.BuildConfig;
import com.hon.mylogger.MyCrashHandler;
import com.hon.mylogger.MyLogger;

/**
 * Created by Frank_Hon on 1/6/2020.
 * E-mail: v-shhong@microsoft.com
 */
public class Fantasy extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

//        registerActivityLifecycleCallbacks(new FantasyActivityLifecycleCallback());

        createNotificationChannel();

        MyLogger.setLoggable(BuildConfig.DEBUG);
        //将日志记录到本地
        MyLogger.initLogFilePath(getFilesDir().getPath());
        //记录崩溃日志
        MyCrashHandler.init(BuildConfig.VERSION_NAME);
    }

    public static Context getAppContext() {
        return sContext;
    }
}
