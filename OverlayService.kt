package com.sohag.displaymask

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.WindowManager

class OverlayService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var wm: WindowManager
    private lateinit var prefs: SharedPreferences
    private var view: MaskView? = null

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(1, buildNotification())
        if (view == null) addOverlay()
        return START_STICKY
    }

    private fun addOverlay() {
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefs = getSharedPreferences("mask", MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(this)
        val v = MaskView(this, prefs)
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        if (Build.VERSION.SDK_INT >= 28) {
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        wm.addView(v, lp)
        view = v
    }

    override fun onSharedPreferenceChanged(p: SharedPreferences?, key: String?) {
        view?.invalidate()
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("mask", "Display Mask", NotificationManager.IMPORTANCE_LOW)
        )
        val stop = PendingIntent.getService(
            this, 0, Intent(this, OverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val open = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, "mask")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("Display Mask চালু আছে")
            .setContentText("সেটিং বদলাতে ট্যাপ করুন")
            .setContentIntent(open)
            .addAction(Notification.Action.Builder(null, "বন্ধ করুন", stop).build())
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        if (::prefs.isInitialized) prefs.unregisterOnSharedPreferenceChangeListener(this)
        view?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        view = null
        super.onDestroy()
    }

    companion object { const val ACTION_STOP = "com.sohag.displaymask.STOP" }
}
