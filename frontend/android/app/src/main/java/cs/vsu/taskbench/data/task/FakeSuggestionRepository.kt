package cs.vsu.taskbench.data.task

import android.util.Log
import cs.vsu.taskbench.domain.model.AiSuggestions
import cs.vsu.taskbench.util.Lipsum
import java.time.LocalDateTime
import kotlin.random.Random

object FakeSuggestionRepository : SuggestionRepository {
    private val TAG = FakeSuggestionRepository::class.simpleName

    private val CATEGORIES = listOf("work", "home", "hobby", "school", "lorem", "ipsum", "dolor")

    override suspend fun getSuggestions(prompt: String): AiSuggestions {
        Log.d(TAG, "requested suggestions")

        if (prompt.isBlank()) {
            Log.d(TAG, "empty prompt, returning nothing")
            return AiSuggestions()
        }

        val random = Random(prompt.hashCode())
        val result = mutableListOf<String>()
        repeat(random.nextInt(1, 7)) {
            result += "${it + 1}. ${Lipsum.get(4, 11, random)}"
        }

        val deadline = if (random.nextBoolean()) {
            LocalDateTime.now().plusDays(random.nextLong(0, 6))
        } else null
        val isHighPriority = if (random.nextBoolean()) random.nextBoolean() else null
        val suggestions = AiSuggestions(
            subtasks = result,
            deadline = deadline,
            isHighPriority = isHighPriority,
            categoryName = if (random.nextBoolean()) null else CATEGORIES.random(random),
        )

        Log.d(TAG, "returned ${result.size} suggestions")
        return suggestions
    }
}
