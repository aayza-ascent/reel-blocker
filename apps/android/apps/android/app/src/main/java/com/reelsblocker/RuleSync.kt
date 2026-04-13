package com.reelsblocker

import android.content.Context
import androidx.work.*
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class RuleSyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    override suspend fun doWork(): Result {
        return try {
            val response = client.get(
                "${BuildConfig.RULE_SERVER_URL}/rules?platform=android&schemaVersion=${SCHEMA_VERSION}"
            )
            when (response.status.value) {
                200 -> {
                    val rules = Json { ignoreUnknownKeys = true }
                        .decodeFromString(RuleSet.serializer(), response.bodyAsText())
                    RuleCache.save(applicationContext, rules)
                    Result.success()
                }
                422 -> Result.failure()
                else -> Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val SCHEMA_VERSION = 1

        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<RuleSyncWorker>(4, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "rule_sync", ExistingPeriodicWorkPolicy.KEEP, req)
        }

        fun getCached(context: Context): AndroidRules =
            RuleCache.load(context)?.android ?: DefaultRules.android
    }
}
