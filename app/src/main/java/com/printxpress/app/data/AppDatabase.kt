package com.printxpress.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.printxpress.app.data.dao.AddressDao
import com.printxpress.app.data.dao.NotificationDao
import com.printxpress.app.data.dao.OrderDao
import com.printxpress.app.data.dao.ProductDao
import com.printxpress.app.data.dao.SavedDesignDao
import com.printxpress.app.data.dao.SessionDao
import com.printxpress.app.data.dao.UserDao
import com.printxpress.app.data.model.AddressEntity
import com.printxpress.app.data.model.NotificationEntity
import com.printxpress.app.data.model.OrderEntity
import com.printxpress.app.data.model.ProductEntity
import com.printxpress.app.data.model.SavedDesignEntity
import com.printxpress.app.data.model.SessionEntity
import com.printxpress.app.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        AddressEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        SavedDesignEntity::class,
        NotificationEntity::class,
        SessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun addressDao(): AddressDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun savedDesignDao(): SavedDesignDao
    abstract fun notificationDao(): NotificationDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "printxpress.db"
                )
                    .addCallback(SeedCallback())
                    .build()
                    .also { instance = it }
            }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                instance?.productDao()?.upsertAll(ProductCatalog.seedProducts)
            }
        }
    }
}
