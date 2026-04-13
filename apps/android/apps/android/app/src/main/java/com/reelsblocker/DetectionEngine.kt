package com.reelsblocker

import android.content.Context
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class DetectionEngine(private val context: Context) {
    private var rules: AndroidRules = RuleSyncWorker.getCached(context)

    data class DetectionResult(
        val verdict: Verdict,
        val confidence: Float,
        val matchedLayer: Int,
        val debugInfo: String
    )

    fun evaluate(event: AccessibilityEvent, root: AccessibilityNodeInfo?): DetectionResult {
        rules = RuleSyncWorker.getCached(context)
        l1TabDetection(event)?.let { return it }
        if (root == null) return DetectionResult(Verdict.UNCERTAIN, 0f, 0, "null root")
        l2IdMatch(root)?.let { return it }
        l3StructuralMatch(root)?.let { return it }
        return l4HeuristicMatch(root) ?: DetectionResult(Verdict.CLEAR, 1f, 0, "no match")
    }

    private fun l1TabDetection(event: AccessibilityEvent): DetectionResult? {
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_CLICKED) return null
        val srcId = event.source?.viewIdResourceName ?: return null
        if (rules.tabBarReelIds.any { srcId.contains(it) })
            return DetectionResult(Verdict.REELS_DETECTED, 0.95f, 1, "tab:$srcId")
        return null
    }

    private fun l2IdMatch(root: AccessibilityNodeInfo): DetectionResult? {
        for (id in rules.reelContainerIds) {
            val nodes = root.findAccessibilityNodeInfosByViewId("com.instagram.android:id/$id")
            if (nodes.isNotEmpty())
                return DetectionResult(Verdict.REELS_DETECTED, 0.90f, 2, "id:$id")
        }
        val descPatterns = rules.reelContentDescriptions.map { Regex(it, RegexOption.IGNORE_CASE) }
        if (findNodeByDescriptionPattern(root, descPatterns) != null)
            return DetectionResult(Verdict.REELS_DETECTED, 0.85f, 2, "contentDesc")
        return null
    }

    private fun l3StructuralMatch(root: AccessibilityNodeInfo): DetectionResult? {
        for (sig in rules.structuralSignatures) {
            if (findStructuralMatch(root, sig) != null)
                return DetectionResult(Verdict.REELS_DETECTED, 0.80f, 3, "structural:${sig.parentClass}")
        }
        return null
    }

    private fun l4HeuristicMatch(root: AccessibilityNodeInfo): DetectionResult? {
        val screenBounds = Rect(); root.getBoundsInScreen(screenBounds)
        val videoNodes = mutableListOf<AccessibilityNodeInfo>()
        findVideoNodes(root, videoNodes)
        val fullscreenVideo = videoNodes.any { node ->
            val b = Rect(); node.getBoundsInScreen(b)
            b.width().toFloat() / screenBounds.width() > 0.9f &&
            b.height().toFloat() / screenBounds.height() > 0.85f &&
            b.height().toFloat() / b.width() > 1.5f
        }
        return if (fullscreenVideo)
            DetectionResult(Verdict.REELS_DETECTED, 0.70f, 4, "heuristic:fullscreen-video")
        else null
    }

    private fun findNodeByDescriptionPattern(
        node: AccessibilityNodeInfo,
        patterns: List<Regex>
    ): AccessibilityNodeInfo? {
        val desc = node.contentDescription?.toString() ?: ""
        if (patterns.any { it.containsMatchIn(desc) }) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findNodeByDescriptionPattern(child, patterns)?.let { return it }
        }
        return null
    }

    private fun findStructuralMatch(
        node: AccessibilityNodeInfo,
        sig: StructuralSignature
    ): AccessibilityNodeInfo? {
        if (node.className?.contains(sig.parentClass) == true && node.childCount >= sig.minChildCount) {
            val childClasses = (0 until node.childCount).mapNotNull { node.getChild(it)?.className?.toString() }
            if (sig.childPattern.all { pattern -> childClasses.any { it.contains(pattern) } })
                return node
        }
        for (i in 0 until node.childCount) {
            findStructuralMatch(node.getChild(i) ?: continue, sig)?.let { return it }
        }
        return null
    }

    private fun findVideoNodes(node: AccessibilityNodeInfo, results: MutableList<AccessibilityNodeInfo>) {
        if (node.className?.contains("VideoView") == true ||
            node.className?.contains("SurfaceView") == true) results.add(node)
        for (i in 0 until node.childCount) findVideoNodes(node.getChild(i) ?: continue, results)
    }
}
