package cs.vsu.taskbench.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.FakeAuthService
import cs.vsu.taskbench.data.auth.network.NetworkAuthService
import cs.vsu.taskbench.data.auth.network.NetworkAuthenticator
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.data.category.FakeCategoryRepository
import cs.vsu.taskbench.data.category.network.NetworkCategoryDataSource
import cs.vsu.taskbench.data.category.network.NetworkCategoryRepository
import cs.vsu.taskbench.data.statistics.FakeStatisticsRepository
import cs.vsu.taskbench.data.statistics.StatisticsRepository
import cs.vsu.taskbench.data.statistics.network.NetworkStatisticsDataSource
import cs.vsu.taskbench.data.statistics.network.NetworkStatisticsRepository
import cs.vsu.taskbench.data.task.FakeTaskRepository
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.data.task.network.NetworkTaskDataSource
import cs.vsu.taskbench.data.task.network.NetworkTaskRepository
import cs.vsu.taskbench.data.task.suggestions.FakeSuggestionRepository
import cs.vsu.taskbench.data.task.suggestions.SuggestionRepository
import cs.vsu.taskbench.data.task.suggestions.network.NetworkSuggestionDataSource
import cs.vsu.taskbench.data.task.suggestions.network.NetworkSuggestionRepository
import cs.vsu.taskbench.data.user.FakeUserRepository
import cs.vsu.taskbench.data.user.UserRepository
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private val Context.dataStore by preferencesDataStore("settings")

private fun Module.fakeUser() {
    singleOf(::FakeUserRepository) bind UserRepository::class
}

private fun Module.fakeAuth() {
    singleOf(::FakeAuthService) bind AuthService::class
}

private fun Module.netAuth() {
    single { get<Retrofit>().create(NetworkAuthenticator::class.java) }
    singleOf(::NetworkAuthService) bind AuthService::class
}

private fun Module.fakeCategories() {
    single { FakeCategoryRepository } bind CategoryRepository::class
}

private fun Module.netCategories() {
    single { get<Retrofit>().create(NetworkCategoryDataSource::class.java) }
    singleOf(::NetworkCategoryRepository) bind CategoryRepository::class
}

private fun Module.fakeStatistics() {
    single { FakeStatisticsRepository } bind StatisticsRepository::class
}

private fun Module.netStatistics() {
    single { get<Retrofit>().create(NetworkStatisticsDataSource::class.java) }
    singleOf(::NetworkStatisticsRepository) bind StatisticsRepository::class
}

private fun Module.fakeSuggestions() {
    singleOf(::FakeSuggestionRepository) bind SuggestionRepository::class
}

private fun Module.netSuggestions() {
    single { get<Retrofit>().create(NetworkSuggestionDataSource::class.java) }
    singleOf(::NetworkSuggestionRepository) bind SuggestionRepository::class
}

private fun Module.fakeTasks() {
    singleOf(::FakeTaskRepository) bind TaskRepository::class
}

private fun Module.netTasks() {
    single { get<Retrofit>().create(NetworkTaskDataSource::class.java) }
    singleOf(::NetworkTaskRepository) bind TaskRepository::class
}

private const val SERVER_ADDRESS = "193.135.137.154"

val dataModule = module {
    single { get<Context>().dataStore }
    single { Moshi.Builder().build() }
    single {
        OkHttpClient.Builder()
            .hostnameVerifier { hostname, _ ->
                hostname == SERVER_ADDRESS
            }
            .build()
    }
    single {
        Retrofit.Builder()
            .client(get())
            .baseUrl("https://$SERVER_ADDRESS/")
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

//    fakeAuth()
    fakeUser()
//    fakeCategories()
//    fakeSuggestions()
//    fakeTasks()
//    fakeStatistics()

    netAuth()
//    netUser()
    netCategories()
    netSuggestions()
    netTasks()
    netStatistics()
}
