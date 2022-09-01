package com.frankhon.fantasymusic.application;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Frank_Hon on 7/23/2019.
 * E-mail: v-shhong@microsoft.com
 * <p>
 * Global executor pools for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
public class AppExecutors {

    private static volatile AppExecutors INSTANCE;

    private final Executor diskIO;
    private final Executor networkIO;
    private final Executor mainThread;

    private AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    public static AppExecutors getInstance() {
        if (INSTANCE == null) {
            synchronized (AppExecutors.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppExecutors(
                            Executors.newSingleThreadExecutor(),
                            Executors.newFixedThreadPool(3),
                            new MainThreadExecutor()
                    );
                }
            }
        }

        return INSTANCE;
    }

    public Executor getDiskIO() {
        return diskIO;
    }

    public Executor getNetworkIO() {
        return networkIO;
    }

    public Executor getMainThread() {
        return mainThread;
    }

    static final class MainThreadExecutor implements Executor {

        private final Handler mainThreadHandler;

        private MainThreadExecutor() {
            this.mainThreadHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
