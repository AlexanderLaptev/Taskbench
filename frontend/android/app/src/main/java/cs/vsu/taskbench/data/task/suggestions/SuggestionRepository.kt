package cs.vsu.taskbench.data.task.suggestions

import cs.vsu.taskbench.domain.model.AiSuggestions

interface SuggestionRepository {
    suspend fun getSuggestions(prompt: String): AiSuggestions
}
