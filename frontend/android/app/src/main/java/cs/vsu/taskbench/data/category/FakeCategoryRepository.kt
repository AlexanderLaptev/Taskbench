package cs.vsu.taskbench.data.category

import androidx.collection.mutableIntObjectMapOf
import cs.vsu.taskbench.domain.model.Category

object FakeCategoryRepository : CategoryRepository {
    private val categories = mutableIntObjectMapOf<Category>().apply {
        this[1] = Category(1, "work")
        this[2] = Category(2, "home")
        this[3] = Category(3, "hobbies")
    }

    private var id = 4

    override suspend fun preload(): Boolean = true

    override suspend fun getAllCategories(): List<Category> {
        val result = mutableListOf<Category>()
        categories.forEachValue { result += it }
        return result
    }

    override suspend fun saveCategory(category: Category): Category {
        if (category.id == null) {
            val copy = category.copy(id = id)
            categories[id] = copy
            id++
            return copy
        } else {
            check(categories[category.id] == null) { "Attempted to modify a non-existent category" }
            categories[category.id] = category
            return category
        }
    }

    override suspend fun deleteCategory(category: Category) {
        check(category.id != null) { "Attempted to delete a non-existent category" }
        categories.remove(category.id)
    }
}
