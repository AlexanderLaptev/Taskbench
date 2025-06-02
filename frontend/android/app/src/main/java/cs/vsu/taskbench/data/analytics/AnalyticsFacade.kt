package cs.vsu.taskbench.data.analytics

import cs.vsu.taskbench.BuildConfig
import io.appmetrica.analytics.AppMetrica

object AnalyticsFacade {
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
    }

    @Suppress("SameParameterValue")
    private fun reportEvent(eventName: String) {
        if (BuildConfig.DEBUG) return
        AppMetrica.reportEvent(eventName)
    }

    private fun reportError(tag: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) return
        AppMetrica.reportError(tag, throwable)
    }
}
