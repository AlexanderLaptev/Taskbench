package cs.vsu.taskbench.data.category.network

import com.squareup.moshi.JsonClass
import cs.vsu.taskbench.domain.model.Category
import cs.vsu.taskbench.util.HttpHeaders
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class CategoryCreateRequest(
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
}
