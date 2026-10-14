package com.printxpress.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.printxpress.app.data.AppDatabase;
import com.printxpress.app.data.PrintXpressRepository;
import com.printxpress.app.util.AppExecutors;

public class PrintXpressApp extends Application {
    public static final String CHANNEL_ID = "printxpress_updates";

    private PrintXpressRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase database = AppDatabase.get(this);
        repository = new PrintXpressRepository(database);
        AppExecutors.get().diskIo().execute(repository::ensureSeeded);
        createNotificationChannel();
    }

    public PrintXpressRepository getRepository() {
        return repository;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "PrintXpress updates",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Order progress and promotional offers");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
