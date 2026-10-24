package com.sohag.displaymask

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

/** Draws up to 3 solid vertical strips. Pure black = OLED pixels off, so it blends with the bezel. */
class MaskView(ctx: Context, private val prefs: SharedPreferences) : View(ctx) {
    private val paint = Paint().apply { style = Paint.Style.FILL }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val alpha = (prefs.getInt("alpha", 100) * 255 / 100).coerceIn(0, 255)
        paint.color = Color.argb(alpha, 0, 0, 0)
        for (i in 0 until COUNT) {
            if (!prefs.getBoolean("s${i}_on", false)) continue
            val sw = w * prefs.getInt("s${i}_w", 2) / 100f
            val left = (w - sw) * prefs.getInt("s${i}_x", 50) / 100f
            c.drawRect(left, 0f, left + sw, h, paint)
        }
    }

    companion object { const val COUNT = 3 }
}
