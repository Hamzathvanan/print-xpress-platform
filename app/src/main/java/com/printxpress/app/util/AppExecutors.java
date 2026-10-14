package com.printxpress.app.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class AppExecutors {
    private static final AppExecutors INSTANCE = new AppExecutors();

    private final Executor diskIo = Executors.newSingleThreadExecutor();
    private final Executor mainThread = new MainThreadExecutor();

    private AppExecutors() {
    }

    public static AppExecutors get() {
        return INSTANCE;
    }

    public Executor diskIo() {
        return diskIo;
    }

    public Executor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }
}
