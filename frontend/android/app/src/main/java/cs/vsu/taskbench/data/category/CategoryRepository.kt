package cs.vsu.taskbench.data.category

import cs.vsu.taskbench.model.Category

interface CategoryRepository {
    suspend fun getAllCategories(): List<Category>

    suspend fun saveCategory(category: Category): Category
}
