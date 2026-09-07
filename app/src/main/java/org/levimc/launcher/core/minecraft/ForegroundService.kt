package org.levimc.launcher.core.minecraft

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log

class ForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        createChannel()
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(org.levimc.launcher.R.drawable.ic_notification_leaf)
            .setContentTitle("LeviLaunchroid")
            .setContentText("Minecraft is still running in background")
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to enter foreground", t)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "LeviLaunchroid: Minecraft Is Running",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = ""
                setShowBadge(false)
            }
        )
    }

    companion object {
        private const val TAG = "LL FGS"
        private const val CHANNEL_ID = "levilaunchroid_foreground_service"
        private const val NOTIFICATION_ID = 2026

        fun runMinecraftInBackground(context: Context) {
            val app = context.applicationContext
            val intent = Intent(app, ForegroundService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    app.startForegroundService(intent)
                } else {
                    app.startService(intent)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Unable to start foreground service", t)
            }
        }

        fun stopForegroundService(context: Context) {
            val app = context.applicationContext
            try {
                app.stopService(Intent(app, ForegroundService::class.java))
            } catch (t: Throwable) {
                Log.w(TAG, "Unable to stop foreground service", t)
            }
        }
    }
}
