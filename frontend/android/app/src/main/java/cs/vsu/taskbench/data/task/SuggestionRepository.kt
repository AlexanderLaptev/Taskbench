package cs.vsu.taskbench.data.task

interface SuggestionRepository {
    suspend fun getSuggestions(prompt: String): List<String>
}
