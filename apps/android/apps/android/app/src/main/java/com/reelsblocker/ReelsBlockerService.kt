package com.reelsblocker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent

class ReelsBlockerService : AccessibilityService() {
    private val detectionEngine by lazy { DetectionEngine(this) }
    private val overlayRenderer by lazy { OverlayRenderer(this) }

    override fun onServiceConnected() {
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                         AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                         AccessibilityEvent.TYPE_VIEW_CLICKED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            packageNames = arrayOf("com.instagram.android")
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName != "com.instagram.android") return
        val result = detectionEngine.evaluate(event, rootInActiveWindow)
        when (result.verdict) {
            Verdict.REELS_DETECTED -> overlayRenderer.show(result.confidence)
            Verdict.CLEAR -> overlayRenderer.hide()
            Verdict.UNCERTAIN -> {}
        }
    }

    override fun onInterrupt() = overlayRenderer.hide()
}
