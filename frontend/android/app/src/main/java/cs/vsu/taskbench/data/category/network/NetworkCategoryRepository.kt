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

        private val deletedCategoryIds = mutableSetOf<Int>()
    }

    private var cache: MutableList<Category> = mutableListOf()

    override suspend fun preload() {
        authService.withAuth {
            cache.clear()
            val allCategories = dataSource.getAllCategories(it)
            
            val filteredCategories = allCategories.filter { category ->
                category.id == null || category.id !in deletedCategoryIds
            }
            
            cache.addAll(filteredCategories)
        }
        Log.d(
            TAG,
            "preload: cache size: ${cache.size}, excluded categories: ${deletedCategoryIds.size}"
        )
    }

    override suspend fun getAllCategories(query: String): List<Category> {
        Log.d(TAG, "getAllCategories: searching categories with query='$query'")
        
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
        
        if (category.id != null) {
            return updateCategory(category)
        }
        
        var saved: Category? = null
        authService.withAuth {
            saved = dataSource.createCategory(it, CategoryCreateRequest(category.name))
        }
        preload()
        return saved!!
    }
    
    override suspend fun updateCategory(category: Category): Category {
        if (category.id == null) {
            Log.w(TAG, "updateCategory: cannot update category without ID, creating a new one instead")
            return saveCategory(category)
        }
        
        Log.d(TAG, "updateCategory: updating category $category")
        var updated: Category? = null
        
        try {
            authService.withAuth {
                updated = dataSource.updateCategory(it, category.id, CategoryUpdateRequest(category.name))
            }
            
            if (updated != null) {
                val index = cache.indexOfFirst { it.id == category.id }
                if (index != -1) {
                    cache[index] = updated!!
                    Log.d(TAG, "updateCategory: updated cache at index $index with $updated")
                } else {
                    cache.add(updated!!)
                    Log.d(TAG, "updateCategory: added updated category to cache: $updated")
                }
            }
            
            Log.d(TAG, "updateCategory: successfully updated category to $updated")
        } catch (e: Exception) {
            Log.e(TAG, "updateCategory: failed to update category", e)
            throw e
        }
        
        return updated!!
    }

    override suspend fun deleteCategory(category: Category) {
        if (category.id == null) {
            Log.w(TAG, "deleteCategory: cannot delete category without ID")
            return
        }
        
        try {
            authService.withAuth {
                dataSource.deleteCategory(it, category.id)
            }
            cache.removeAll { it.id == category.id }
            deletedCategoryIds.add(category.id)
            Log.d(TAG, "deleteCategory: successfully deleted category $category")
        } catch (e: Exception) {
            Log.e(TAG, "deleteCategory: failed to delete category via API, using local deletion", e)
            cache.removeAll { it.id == category.id }
            deletedCategoryIds.add(category.id)
            Log.d(TAG, "deleteCategory: locally deleted category $category")
        }
    }
}
