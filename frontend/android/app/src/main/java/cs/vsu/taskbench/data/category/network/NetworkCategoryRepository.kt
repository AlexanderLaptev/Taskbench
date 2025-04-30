package cs.vsu.taskbench.data.category.network

import android.util.Log
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.withAuth
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.util.toAuthHeader

class NetworkCategoryRepository(
    private val authService: AuthService,
    private val dataSource: NetworkCategoryDataSource,
) : CategoryRepository {
    companion object {
        private val TAG = NetworkCategoryRepository::class.simpleName
    }

    private var cache: MutableList<Category> = mutableListOf()

    override suspend fun preload() {
        authService.withAuth {
            cache.clear()
            cache.addAll(dataSource.getAllCategories(it.access.toAuthHeader()))
        }
        Log.d(TAG, "preload: cache size: ${cache.size}")
    }

    override suspend fun getAllCategories(query: String): List<Category> {
        Log.d(TAG, "getAllCategories: searching categories with query='$query'")
        return if (query.isBlank()) cache
        else {
            val lowerQuery = query.lowercase()
            cache.filter { lowerQuery in it.name.lowercase() }
        }
    }

    override suspend fun saveCategory(category: Category): Category {
        Log.d(TAG, "saveCategory: saving category $category")
        var saved: Category? = null
        authService.withAuth {
            saved = dataSource.createCategory(
                it.access.toAuthHeader(),
                CategoryCreateRequest(category.name),
            )
        }
        preload()
        return saved!!
    }

    override suspend fun deleteCategory(category: Category) {
        // TODO
        Log.w(TAG, "deleteCategory: deleting categories is not implemented")
    }
}
