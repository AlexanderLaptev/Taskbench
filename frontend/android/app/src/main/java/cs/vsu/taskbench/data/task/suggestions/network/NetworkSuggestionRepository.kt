package cs.vsu.taskbench.data.task.suggestions.network

import android.util.Log
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.withAuth
import cs.vsu.taskbench.data.task.network.TaskDpc
import cs.vsu.taskbench.data.task.suggestions.SuggestionRepository
import cs.vsu.taskbench.domain.model.AiSuggestions
import cs.vsu.taskbench.domain.model.Category
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NetworkSuggestionRepository(
    private val authService: AuthService,
    private val dataSource: NetworkSuggestionDataSource,
) : SuggestionRepository {
    companion object {
        private val TAG = NetworkSuggestionRepository::class.simpleName
    }

    override suspend fun getSuggestions(
        prompt: String,
        deadline: LocalDateTime?,
        isHighPriority: Boolean,
        category: Category?,
    ): AiSuggestions {
        Log.d(TAG, "getSuggestions: getting suggestions")
        Log.d(TAG, "getSuggestions: prompt='$prompt'")
        Log.d(TAG, "getSuggestions: deadline='$deadline'")
        Log.d(TAG, "getSuggestions: priority='$isHighPriority'")
        Log.d(TAG, "getSuggestions: category='$category'")

        authService.withAuth { access ->
            val request = SuggestionsRequest(
                title = prompt,
                timestamp = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()),
                dpc = TaskDpc(
                    deadline = deadline?.let { DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(it) },
                    priority = if (isHighPriority) 1 else 0,
                    category_id = category?.id,
                    category_name = category?.name,
                ),
            )

            val response = dataSource.getSuggestions(access, request)
            val suggestedPriority = response.suggested_dpc.priority?.let { it == 1 }
            val suggestedDeadline = response.suggested_dpc.deadline?.let {
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(it) as LocalDateTime
            }
            val suggestedCategory = with(response.suggested_dpc) {
                if (category_id != null && category_name != null) {
                    Category(category_id, category_name)
                } else {
                    null
                }
            }

            return AiSuggestions(
                subtasks = response.suggestions,
                isHighPriority = suggestedPriority,
                deadline = suggestedDeadline,
                category = suggestedCategory,
            )
        }
        error("unreachable code")
    }
}
