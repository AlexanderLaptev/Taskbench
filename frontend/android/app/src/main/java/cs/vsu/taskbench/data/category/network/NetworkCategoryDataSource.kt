package cs.vsu.taskbench.data.category.network

import com.squareup.moshi.JsonClass
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.util.HttpHeaders
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class CategoryCreateRequest(
    val name: String,
)

@JsonClass(generateAdapter = true)
data class CategoryUpdateRequest(
    val name: String,
)

interface NetworkCategoryDataSource {
    @GET("categories/")
    suspend fun getAllCategories(@Header(HttpHeaders.AUTHORIZATION) auth: String): List<Category>

    @POST("categories/")
    suspend fun createCategory(
        @Header(HttpHeaders.AUTHORIZATION) auth: String,
        @Body request: CategoryCreateRequest,
    ): Category

    @PATCH("categories/{id}/")
    suspend fun updateCategory(
        @Header(HttpHeaders.AUTHORIZATION) auth: String,
        @Path("id") id: Int,
        @Body request: CategoryUpdateRequest,
    ): Category

    @DELETE("categories/{id}/")
    suspend fun deleteCategory(
        @Header(HttpHeaders.AUTHORIZATION) auth: String,
        @Path("id") id: Int
    )
}
