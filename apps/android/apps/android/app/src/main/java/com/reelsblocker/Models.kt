package com.reelsblocker

import kotlinx.serialization.Serializable

@Serializable
data class RuleSet(
    val schemaVersion: Int,
    val instagramVersion: String,
    val publishedAt: String,
    val android: AndroidRules,
    val ios: IosRules
)

@Serializable
data class AndroidRules(
    val tabBarReelIds: List<String>,
    val reelContainerIds: List<String>,
    val reelContentDescriptions: List<String>,
    val structuralSignatures: List<StructuralSignature>
)

@Serializable
data class StructuralSignature(
    val parentClass: String,
    val childPattern: List<String>,
    val minChildCount: Int,
    val aspectRatioMin: Float
)

@Serializable
data class IosRules(
    val cssSelectors: List<String>,
    val urlBlockPatterns: List<String>
)

enum class Verdict { REELS_DETECTED, CLEAR, UNCERTAIN }

object DefaultRules {
    val android = AndroidRules(
        tabBarReelIds = listOf("clips_tab", "reels_tab", "navigation_reels"),
        reelContainerIds = listOf("clips_viewer_fragment_root", "reel_viewer_root"),
        reelContentDescriptions = listOf("reel", "clip"),
        structuralSignatures = emptyList()
    )
}
