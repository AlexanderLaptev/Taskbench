package cs.vsu.taskbench.data.analytics

import io.appmetrica.analytics.AppMetrica

object AnalyticsFacade {
    fun logScreen(screenName: String) {
        AppMetrica.reportEvent("screen_view", mapOf("screen_name" to screenName))
    }

    fun logEvent(event: String, params: Map<String, Any?> = emptyMap()) {
        AppMetrica.reportEvent(event, params)
    }

    fun logLoginSuccess(email: String) {
        AppMetrica.reportEvent("login_success", mapOf("email" to email))
    }

    fun logLoginFailure(reason: String) {
        AppMetrica.reportEvent("login_failure", mapOf("reason" to reason))
    }

    fun logTaskCreated(taskId: Long?) {
        AppMetrica.reportEvent("task_created", mapOf("task_id" to taskId))
    }

    fun logTaskDeleted(taskId: Long?) {
        AppMetrica.reportEvent("task_deleted", mapOf("task_id" to taskId))
    }

    fun logCategorySelected(category: String) {
        AppMetrica.reportEvent("category_selected", mapOf("category" to category))
    }

    fun logSortChanged(sortBy: String) {
        AppMetrica.reportEvent("sort_changed", mapOf("sort_by" to sortBy))
    }

    fun logPremiumActivated() {
        AppMetrica.reportEvent("premium_activated")
    }

    fun logNavigate(from: String, to: String) {
        AppMetrica.reportEvent("navigate", mapOf("from" to from, "to" to to))
    }

    fun logError(tag: String, throwable: Throwable) {
        AppMetrica.reportError(tag, throwable)
    }
}
