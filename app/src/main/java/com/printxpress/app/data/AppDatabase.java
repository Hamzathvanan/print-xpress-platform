package com.printxpress.app.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.printxpress.app.data.dao.AddressDao;
import com.printxpress.app.data.dao.NotificationDao;
import com.printxpress.app.data.dao.OrderDao;
import com.printxpress.app.data.dao.ProductDao;
import com.printxpress.app.data.dao.SavedDesignDao;
import com.printxpress.app.data.dao.SessionDao;
import com.printxpress.app.data.dao.UserDao;
import com.printxpress.app.data.model.AddressEntity;
import com.printxpress.app.data.model.NotificationEntity;
import com.printxpress.app.data.model.OrderEntity;
import com.printxpress.app.data.model.ProductEntity;
import com.printxpress.app.data.model.SavedDesignEntity;
import com.printxpress.app.data.model.SessionEntity;
import com.printxpress.app.data.model.UserEntity;
import com.printxpress.app.util.AppExecutors;

@Database(
        entities = {
                UserEntity.class,
                AddressEntity.class,
                ProductEntity.class,
                OrderEntity.class,
                SavedDesignEntity.class,
                NotificationEntity.class,
                SessionEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract UserDao userDao();

    public abstract AddressDao addressDao();

    public abstract ProductDao productDao();

    public abstract OrderDao orderDao();

    public abstract SavedDesignDao savedDesignDao();

    public abstract NotificationDao notificationDao();

    public abstract SessionDao sessionDao();

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "printxpress.db"
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(new SeedCallback())
                            .build();
                }
            }
        }
        return instance;
    }

    private static class SeedCallback extends Callback {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            AppExecutors.get().diskIo().execute(() -> {
                AppDatabase database = instance;
                if (database != null) {
                    database.productDao().upsertAll(ProductCatalog.SEED_PRODUCTS);
                }
            });
        }
    }
}
