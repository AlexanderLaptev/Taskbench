package cs.vsu.taskbench.data.category

import android.util.Log
import androidx.collection.mutableIntObjectMapOf
import cs.vsu.taskbench.domain.model.Category

object FakeCategoryRepository : CategoryRepository {
    private val TAG = FakeCategoryRepository::class.simpleName

    private val categories = mutableIntObjectMapOf<Category>().apply {
        this[1] = Category(1, "work")
        this[2] = Category(2, "home")
        this[3] = Category(3, "hobbies")
        this[4] = Category(4, "lorem")
        this[5] = Category(5, "ipsum")
        this[6] = Category(6, "dolor")
        this[7] = Category(7, "sit")
        this[8] = Category(8, "amet")
        this[9] = Category(9, "consectetur")
        this[10] = Category(10, "adipiscing")
    }

    private var id = 11

    override suspend fun preload(): Boolean {
        Log.d(TAG, "preloading categories")
        return true
    }

    override suspend fun getAllCategories(query: String): List<Category> {
        val result = mutableListOf<Category>()
        if (query.isBlank()) {
            Log.d(TAG, "requested categories (blank query)")
            categories.forEachValue { result += it }
        } else {
            Log.d(TAG, "requested categories (non-blank query)")
            categories.forEachValue {
                if (query in it.name) result += it
            }
        }
        Log.d(TAG, "returning ${result.size} categories")
        return result
    }

    override suspend fun saveCategory(category: Category): Category {
        Log.d(TAG, "saving category $category")
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
        Log.d(TAG, "deleting category $category")
        check(category.id != null) { "Attempted to delete a non-existent category" }
        categories.remove(category.id)
    }
}
