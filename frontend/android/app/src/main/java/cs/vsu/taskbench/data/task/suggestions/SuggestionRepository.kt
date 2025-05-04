package cs.vsu.taskbench.data.task.suggestions

import cs.vsu.taskbench.domain.model.AiSuggestions
import cs.vsu.taskbench.domain.model.Category
import java.time.LocalDateTime

interface SuggestionRepository {
    suspend fun getSuggestions(
        prompt: String,
        deadline: LocalDateTime?,
        isHighPriority: Boolean,
        category: Category?,
    ): AiSuggestions
}
