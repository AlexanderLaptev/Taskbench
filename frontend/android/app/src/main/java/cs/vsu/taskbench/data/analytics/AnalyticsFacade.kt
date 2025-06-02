package cs.vsu.taskbench.data.analytics

import android.util.Log
import cs.vsu.taskbench.BuildConfig
import io.appmetrica.analytics.AppMetrica

object AnalyticsFacade {
    private val TAG = AnalyticsFacade::class.simpleName

    fun logScreen(screenName: String) {
        reportEvent("screen_view", mapOf("screen_name" to screenName))
    }

    fun logEvent(event: String, params: Map<String, Any?> = emptyMap()) {
        reportEvent(event, params)
    }

    fun logError(tag: String, throwable: Throwable) {
        reportError(tag, throwable)
    }

    @Suppress("SameParameterValue")
    private fun reportEvent(eventName: String, attributes: Map<String, *>) {
        if (BuildConfig.DEBUG) return
        AppMetrica.reportEvent(eventName, attributes)
        Log.d(TAG, "reportEvent: $eventName {${attributes.entries.joinToString()}}")
    }

    @Suppress("SameParameterValue")
    private fun reportEvent(eventName: String) {
        if (BuildConfig.DEBUG) return
        AppMetrica.reportEvent(eventName)
        Log.d(TAG, "reportEvent: $eventName")
    }

    private fun reportError(tag: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) return
        AppMetrica.reportError(tag, throwable)
        Log.e(TAG, "reportError: $tag")
    }
}
