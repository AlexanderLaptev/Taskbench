package cs.vsu.taskbench.data.task

import cs.vsu.taskbench.domain.model.AiSuggestions

interface SuggestionRepository {
    suspend fun getSuggestions(prompt: String): AiSuggestions
}
