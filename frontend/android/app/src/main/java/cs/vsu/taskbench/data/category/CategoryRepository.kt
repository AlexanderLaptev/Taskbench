package cs.vsu.taskbench.data.category

import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.domain.model.Category

interface CategoryRepository : PreloadRepository {
    suspend fun getAllCategories(query: String = ""): List<Category>
    suspend fun saveCategory(category: Category): Category
    suspend fun updateCategory(category: Category): Category
    suspend fun deleteCategory(category: Category)
}
