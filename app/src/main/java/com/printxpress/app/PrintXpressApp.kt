package com.printxpress.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.printxpress.app.data.AppDatabase
import com.printxpress.app.data.PrintXpressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrintXpressApp : Application() {
    lateinit var repository: PrintXpressRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.get(this)
        repository = PrintXpressRepository(database)
        CoroutineScope(Dispatchers.IO).launch {
            repository.ensureSeeded()
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PrintXpress updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Order progress and promotional offers"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "printxpress_updates"
    }
}
