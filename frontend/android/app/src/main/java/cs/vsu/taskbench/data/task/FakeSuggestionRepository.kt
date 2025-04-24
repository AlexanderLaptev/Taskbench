package cs.vsu.taskbench.data.task

import android.util.Log

object FakeSuggestionRepository : SuggestionRepository {
    private val TAG = FakeSuggestionRepository::class.simpleName

    private val SUGGESTIONS = listOf(
        "Lorem ipsum dolor sit amet",
        "Maecenas faucibus tellus eget",
        "Integer vel venenatis arcu",
    )

    override suspend fun getSuggestions(prompt: String): List<String> {
        Log.d(TAG, "requested suggestions")
        Log.d(TAG, "returned ${SUGGESTIONS.size} suggestions")
        return SUGGESTIONS
    }
}
