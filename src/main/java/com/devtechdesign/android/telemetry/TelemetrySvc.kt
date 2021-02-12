package com.devtechdesign.android.telemetry

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class TelemetrySvc() : Service() {

    private val CHANNEL_ID = "ForegroundService Kotlin"

    companion object {
        val EXTRA_NOTIFICATION_TITLE: String = "notificationTitle"
        val EXTRA_INTENT_CLASS: String = "class"

        fun startService(context: Context, title: String, activity: Class<*>?) {
            val startIntent = Intent(context, TelemetrySvc::class.java)
            startIntent.putExtra(EXTRA_NOTIFICATION_TITLE, title)
            startIntent.putExtra(EXTRA_INTENT_CLASS, activity)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, TelemetrySvc::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationTitle = intent?.getStringExtra(EXTRA_NOTIFICATION_TITLE)
        val intentClass = intent?.getSerializableExtra(EXTRA_INTENT_CLASS) as Class<*>
        createNotificationChannel()
        val notificationIntent = Intent(this, intentClass)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(notificationTitle)
            .setSmallIcon(R.drawable.ic_location_tracking)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        //stopSelf();
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }
}