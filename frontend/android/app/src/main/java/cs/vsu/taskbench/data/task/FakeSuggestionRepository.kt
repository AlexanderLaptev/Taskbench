package cs.vsu.taskbench.data.task

object FakeSuggestionRepository : SuggestionRepository {
    private val SUGGESTIONS = listOf(
        "Lorem ipsum dolor sit amet",
        "Maecenas faucibus tellus eget",
        "Integer vel venenatis arcu",
    )
    override suspend fun getSuggestions(prompt: String): List<String> = SUGGESTIONS
}
