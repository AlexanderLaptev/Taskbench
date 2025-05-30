package cs.vsu.taskbench.data.category

import android.util.Log
import androidx.collection.mutableIntObjectMapOf
import cs.vsu.taskbench.domain.model.Category

object FakeCategoryRepository : CategoryRepository {
    private val TAG = FakeCategoryRepository::class.simpleName

    private var id = 1
    private val categories = mutableIntObjectMapOf<Category>()

    init {
        resetCategories()
    }

    private fun resetCategories() {
        categories.clear()
        val names = listOf("Работа", "Дом", "Машина", "Дети")
        for (name in names) {
            categories[id] = Category(id, name)
            id++
        }
    }

    override suspend fun preload() {
        Log.d(TAG, "preloading categories")
    }

    override suspend fun getAllCategories(query: String): List<Category> {
        val result = mutableListOf<Category>()
        if (query.isBlank()) {
            Log.d(TAG, "getAllCategories: returning all categories")
            categories.forEachValue { result += it }
        } else {
            Log.d(TAG, "getAllCategories: filtering categories")
            val queryLower = query.lowercase()
            categories.forEachValue {
                if (queryLower in it.name.lowercase()) result += it
            }
        }
        Log.d(TAG, "returning ${result.size} results")
        return result
    }

    override suspend fun saveCategory(category: Category): Category {
        if (category.id == null) {
            Log.d(TAG, "saveCategory: creating a new one")
            val copy = category.copy(id = id)
            categories[id] = copy
            id++
            return copy
        } else {
            return updateCategory(category)
        }
    }

    override suspend fun updateCategory(category: Category): Category {
        if (category.id == null) {
            Log.d(TAG, "updateCategory: cannot update category without ID, creating a new one instead")
            return saveCategory(category)
        }
        
        Log.d(TAG, "updateCategory: updating category $category")
        
        if (categories.containsKey(category.id)) {
            categories[category.id] = category
            Log.d(TAG, "updateCategory: successfully updated category to $category")
        } else {
            Log.w(TAG, "updateCategory: category with ID ${category.id} not found, creating a new one")
            return saveCategory(category.copy(id = null))
        }
        
        return category
    }

    override suspend fun deleteCategory(category: Category) {
        Log.d(TAG, "deleteCategory: enter")
        check(category.id != null) { "Attempted to delete a non-existent category" }
        categories.remove(category.id)
    }
}

