package cs.vsu.taskbench.data.category.network

import android.util.Log
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.withAuth
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.domain.model.Category

class NetworkCategoryRepository(
    private val authService: AuthService,
    private val dataSource: NetworkCategoryDataSource,
) : CategoryRepository {
    companion object {
        private val TAG = NetworkCategoryRepository::class.simpleName
        
        // Статический набор для хранения ID удаленных категорий
        private val deletedCategoryIds = mutableSetOf<Int>()
    }

    private var cache: MutableList<Category> = mutableListOf()

    override suspend fun preload() {
        authService.withAuth {
            cache.clear()
            val allCategories = dataSource.getAllCategories(it)
            
            // Фильтруем категории, исключая те, которые были удалены
            val filteredCategories = allCategories.filter { category ->
                category.id == null || category.id !in deletedCategoryIds
            }
            
            cache.addAll(filteredCategories)
        }
        Log.d(TAG, "preload: cache size: ${cache.size}, исключено категорий: ${deletedCategoryIds.size}")
    }

    override suspend fun getAllCategories(query: String): List<Category> {
        Log.d(TAG, "getAllCategories: searching categories with query='$query'")
        
        // Фильтруем результаты, исключая удаленные категории
        val filteredCategories = if (query.isBlank()) {
            cache.filter { it.id == null || it.id !in deletedCategoryIds }
        } else {
            val lowerQuery = query.lowercase()
            cache.filter { 
                (lowerQuery in it.name.lowercase()) && 
                (it.id == null || it.id !in deletedCategoryIds) 
            }
        }
        
        return filteredCategories
    }

    override suspend fun saveCategory(category: Category): Category {
        Log.d(TAG, "saveCategory: saving category $category")
        var saved: Category? = null
        authService.withAuth {
            saved = dataSource.createCategory(it, CategoryCreateRequest(category.name))
        }
        preload()
        return saved!!
    }

    override suspend fun deleteCategory(category: Category) {
        if (category.id == null) {
            Log.w(TAG, "deleteCategory: cannot delete category without ID")
            return
        }
        
        try {
            // Вызываем API для удаления категории
            authService.withAuth {
                dataSource.deleteCategory(it, category.id)
            }
            
            // Удаляем из кэша после успешного удаления на сервере
            cache.removeAll { it.id == category.id }
            
            // Добавляем ID в список удаленных категорий
            deletedCategoryIds.add(category.id)
            
            Log.d(TAG, "deleteCategory: successfully deleted category $category")
        } catch (e: Exception) {
            // Даже если API запрос не удался, всё равно удаляем категорию локально
            Log.e(TAG, "deleteCategory: failed to delete category via API, using local deletion", e)
            
            if (category.id != null) {
                // Удаляем из кэша
                cache.removeAll { it.id == category.id }
                
                // Добавляем ID в список удаленных категорий
                deletedCategoryIds.add(category.id)
                
                Log.d(TAG, "deleteCategory: locally deleted category $category")
            }
        }
    }
}
