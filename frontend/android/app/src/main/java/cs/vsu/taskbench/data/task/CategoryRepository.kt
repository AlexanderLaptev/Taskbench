package cs.vsu.taskbench.data.task

import cs.vsu.taskbench.model.Category

interface CategoryRepository {
    suspend fun getAllCategories(): List<Category>

    suspend fun getCategoryById(id: Int): Category?
}
