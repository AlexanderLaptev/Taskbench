package cs.vsu.taskbench.data.task

import android.util.Log
import cs.vsu.taskbench.util.Lipsum
import kotlin.random.Random

object FakeSuggestionRepository : SuggestionRepository {
    private val TAG = FakeSuggestionRepository::class.simpleName

    override suspend fun getSuggestions(prompt: String): List<String> {
        Log.d(TAG, "requested suggestions")

        if (prompt.isBlank()) {
            Log.d(TAG, "empty prompt, returning nothing")
            return listOf()
        }

        val random = Random(prompt.hashCode())
        val result = mutableListOf<String>()
        repeat(random.nextInt(1, 7)) {
            result += "${it + 1}. ${Lipsum.get(4, 11, random)}"
        }

        Log.d(TAG, "returned ${result.size} suggestions")
        return result
    }
}
