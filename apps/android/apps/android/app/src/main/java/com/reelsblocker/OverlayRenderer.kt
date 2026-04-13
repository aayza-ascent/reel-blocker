package com.reelsblocker

import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager

class OverlayRenderer(private val context: Context) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private var overlayView: View? = null

    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        PixelFormat.TRANSLUCENT
    )

    fun show(confidence: Float) {
        if (!Settings.canDrawOverlays(context)) return
        if (overlayView != null) return
        overlayView = LayoutInflater.from(context)
            .inflate(R.layout.overlay_reels_blocker, null)
            .also { windowManager.addView(it, layoutParams) }
    }

    fun hide() {
        overlayView?.let { windowManager.removeViewImmediate(it); overlayView = null }
    }
}
