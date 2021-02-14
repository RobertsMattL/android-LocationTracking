package com.devtechdesign.android.telemetry

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import java.math.BigDecimal
import java.math.RoundingMode

class TelemetrySvc() : Service() {

    private var manager: NotificationManager? = null
    private var notificationBuilder: NotificationCompat.Builder? = null
    private val LOCATION_UPDATE_INTERNAL: Long = 2000
    private var fusedLocationClient: FusedLocationProviderClient? = null
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
        this.notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(notificationTitle)
            .setSmallIcon(R.drawable.ic_location_tracking)
            .setContentIntent(pendingIntent)

        startForeground(1, this.notificationBuilder!!.build())
        return START_NOT_STICKY
    }

    private fun updateNotification(locationText: String) {
        this.notificationBuilder!!.setSubText("$locationText")
        manager?.notify(1, notificationBuilder!!.build())
    }

    override fun onCreate() {
        super.onCreate()
        startLocation()
    }

    private fun startLocation() {
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        var locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest.interval = LOCATION_UPDATE_INTERNAL
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        (fusedLocationClient as FusedLocationProviderClient).requestLocationUpdates(locationRequest, object : LocationCallback() {
            override
            fun onLocationResult(locationResult: LocationResult) {
                onLocationChanged(locationResult.lastLocation)
            }
        }, Looper.myLooper())

    }

    private fun onLocationChanged(lastLocation: Location?) {
        updateNotification("${lastLocation?.latitude},${lastLocation?.longitude}")
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
            this.manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }
}