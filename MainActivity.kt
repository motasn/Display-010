package com.sohag.displaymask

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*

class MainActivity : Activity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        prefs = getSharedPreferences("mask", MODE_PRIVATE)

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = (20 * resources.displayMetrics.density).toInt()
            setPadding(p, p, p, p)
        }
        setContentView(ScrollView(this).apply { addView(col) })

        col.addView(text("Display Mask", 26f))
        col.addView(text(
            "ফোনের সব অ্যাপের উপর কালো স্ট্রিপ বসিয়ে ডিসপ্লের দাগ ঢাকে। " +
            "স্লাইডার নাড়ালে সাথে সাথে বদলায়।", 15f))

        for (i in 0 until MaskView.COUNT) {
            col.addView(text("স্ট্রিপ ${i + 1}", 18f))
            col.addView(Switch(this).apply {
                text = "চালু"
                isChecked = prefs.getBoolean("s${i}_on", false)
                setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("s${i}_on", v).apply() }
            })
            addSlider(col, "অবস্থান (বাম থেকে ডান)", "s${i}_x", 50, 100)
            addSlider(col, "চওড়া (স্ক্রিনের %)", "s${i}_w", 2, 30)
        }
        col.addView(text("স্বচ্ছতা", 18f))
        addSlider(col, "কতটা কালো (100 = পুরো কালো)", "alpha", 100, 100)

        col.addView(Button(this).apply {
            text = "চালু করুন"
            setOnClickListener { start() }
        })
        col.addView(Button(this).apply {
            text = "বন্ধ করুন"
            setOnClickListener {
                startService(Intent(this@MainActivity, OverlayService::class.java)
                    .setAction(OverlayService.ACTION_STOP))
            }
        })
        col.addView(text(
            "টিপস: কালার টেস্টের জন্য ফোনে পুরো সাদা বা লাল ছবি খুলে দাগের জায়গা দেখে নিন, " +
            "তারপর স্ট্রিপ সেই জায়গায় সরান।", 14f))
    }

    private fun text(s: String, size: Float) = TextView(this).apply {
        text = s; textSize = size; setPadding(0, 16, 0, 8)
    }

    private fun addSlider(parent: LinearLayout, label: String, key: String, def: Int, max: Int) {
        parent.addView(TextView(this).apply { text = label })
        parent.addView(SeekBar(this).apply {
            this.max = max
            progress = prefs.getInt(key, def)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, v: Int, u: Boolean) {
                    prefs.edit().putInt(key, v).apply()
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        })
    }

    private fun start() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "অন্য অ্যাপের উপর দেখানোর অনুমতি দিন", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")))
            return
        }
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        startForegroundService(Intent(this, OverlayService::class.java))
    }
}
