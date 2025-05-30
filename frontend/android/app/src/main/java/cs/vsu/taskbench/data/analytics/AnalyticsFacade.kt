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

    fun logLoginSuccess(email: String) {
        reportEvent("login_success", mapOf("email" to email))
    }

    fun logLoginFailure(reason: String) {
        reportEvent("login_failure", mapOf("reason" to reason))
    }

    fun logTaskCreated(taskId: Long?) {
        reportEvent("task_created", mapOf("task_id" to taskId))
    }

    fun logTaskDeleted(taskId: Long?) {
        reportEvent("task_deleted", mapOf("task_id" to taskId))
    }

    fun logCategorySelected(category: String) {
        reportEvent("category_selected", mapOf("category" to category))
    }

    fun logSortChanged(sortBy: String) {
        reportEvent("sort_changed", mapOf("sort_by" to sortBy))
    }

    fun logPremiumActivated() {
        reportEvent("premium_activated")
    }

    fun logNavigate(from: String, to: String) {
        reportEvent("navigate", mapOf("from" to from, "to" to to))
    }

    fun logPremiumScreenView(source: String) {
        reportEvent("premium_screen_view", mapOf("source" to source))
    }

    fun logSubscriptionButtonClick() {
        reportEvent("subscription_button_click")
    }

    fun logSubscriptionError(errorType: String, errorMessage: String) {
        reportEvent("subscription_error", mapOf(
            "error_type" to errorType,
            "error_message" to errorMessage
        ))
    }

    fun logError(tag: String, throwable: Throwable) {
        reportError(tag, throwable)
    }

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
