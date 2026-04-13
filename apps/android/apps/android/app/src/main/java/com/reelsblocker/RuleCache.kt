package com.reelsblocker

import android.content.Context
import kotlinx.serialization.json.Json

object RuleCache {
    private const val PREFS_NAME = "rule_cache"
    private const val KEY_RULES = "cached_rules"

    fun save(context: Context, rules: RuleSet) {
        val json = Json.encodeToString(RuleSet.serializer(), rules)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_RULES, json).apply()
    }

    fun load(context: Context): RuleSet? {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_RULES, null) ?: return null
        return try { Json.decodeFromString(RuleSet.serializer(), json) } catch (e: Exception) { null }
    }
}
