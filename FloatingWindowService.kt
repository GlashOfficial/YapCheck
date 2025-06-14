package com.yapcheck.myapplication

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.yapcheck.myapplication.databinding.OverlayLayoutBinding // Use actual package name

class FloatingWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val verdict = intent?.getStringExtra("verdict") ?: "Unknown"
        val explanation = intent?.getStringExtra("explanation") ?: "No details."

        floatingView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        val verdictText = floatingView.findViewById<TextView>(R.id.verdictText)
        val explanationText = floatingView.findViewById<TextView>(R.id.explanationText)
        val closeBtn = floatingView.findViewById<ImageView>(R.id.closeBtn)

        verdictText.text = verdict
        explanationText.text = explanation

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.x = 0
        params.y = 100

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingView, params)

        closeBtn.setOnClickListener {
            windowManager.removeView(floatingView)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
