package cs.vsu.taskbench.data.task.suggestions

import android.util.Log
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.domain.model.AiSuggestions
import cs.vsu.taskbench.domain.model.Category
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FakeSuggestionRepository(
    private val categoryRepository: CategoryRepository,
) : SuggestionRepository {
    companion object {
        private val TAG = FakeSuggestionRepository::class.simpleName
    }

    private val suggestions = AiSuggestions(
        subtasks = listOf(
            "Выбрать тему праздника",
            "Составить список гостей",
            "Заказать торт",
            "Приготовить подарки",
            "Организовать развлечения или ведущего",
            "Украсить помещение",
            "Подготовить меню и блюда",
            "Проверить наличие необходимой посуды и декора",
            "Составить расписание мероприятий на день рождения",
        ),
        deadline = run {
            val date = LocalDate.now().plusDays(7)
            val time = LocalTime.of(12, 0)
            LocalDateTime.of(date, time)
        },
        isHighPriority = null,
        category = Category(4, "Дети")
    )

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
        return suggestions
    }
}
