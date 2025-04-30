package cs.vsu.taskbench.data.task.suggestions

import android.util.Log
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.domain.model.AiSuggestions
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.util.Lipsum
import java.time.LocalDateTime
import kotlin.random.Random

class FakeSuggestionRepository(
    private val categoryRepository: CategoryRepository,
) : SuggestionRepository {
    companion object {
        private val TAG = FakeSuggestionRepository::class.simpleName
    }

    override suspend fun getSuggestions(
        prompt: String,
        deadline: LocalDateTime?,
        isHighPriority: Boolean,
        category: Category?,
    ): AiSuggestions {
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

        val suggestedDeadline = if (random.nextBoolean()) {
            LocalDateTime.now().plusDays(random.nextLong(0, 6))
        } else null
        val suggestedPriority = if (random.nextBoolean()) random.nextBoolean() else null
        val suggestedCategory = if (random.nextBoolean()) null else {
            val categories = categoryRepository.getAllCategories()
            categories.random(random)
        }
        val suggestions = AiSuggestions(
            subtasks = result,
            deadline = suggestedDeadline,
            isHighPriority = suggestedPriority,
            category = suggestedCategory,
        )

        Log.d(TAG, "returned ${result.size} suggestions")
        return suggestions
    }
}
