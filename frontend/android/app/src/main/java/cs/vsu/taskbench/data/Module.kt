package cs.vsu.taskbench.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.network.NetworkAuthService
import cs.vsu.taskbench.data.auth.network.NetworkAuthenticator
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.data.category.network.NetworkCategoryDataSource
import cs.vsu.taskbench.data.category.network.NetworkCategoryRepository
import cs.vsu.taskbench.data.statistics.StatisticsRepository
import cs.vsu.taskbench.data.statistics.network.NetworkStatisticsDataSource
import cs.vsu.taskbench.data.statistics.network.NetworkStatisticsRepository
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.data.task.network.NetworkTaskDataSource
import cs.vsu.taskbench.data.task.network.NetworkTaskRepository
import cs.vsu.taskbench.data.task.suggestions.SuggestionRepository
import cs.vsu.taskbench.data.task.suggestions.network.NetworkSuggestionDataSource
import cs.vsu.taskbench.data.task.suggestions.network.NetworkSuggestionRepository
import cs.vsu.taskbench.data.user.FakeUserRepository
import cs.vsu.taskbench.data.user.UserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private val Context.dataStore by preferencesDataStore("settings")

val dataModule = module {
    single { get<Context>().dataStore }
    single {
        Moshi.Builder()
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl("http://193.135.137.154:8000/")
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    single { get<Retrofit>().create(NetworkCategoryDataSource::class.java) }
    single { get<Retrofit>().create(NetworkAuthenticator::class.java) }
    single { get<Retrofit>().create(NetworkStatisticsDataSource::class.java) }
    single { get<Retrofit>().create(NetworkSuggestionDataSource::class.java) }
    single { get<Retrofit>().create(NetworkTaskDataSource::class.java) }

    singleOf(::NetworkAuthService) bind AuthService::class
    singleOf(::FakeUserRepository) bind UserRepository::class
    singleOf(::NetworkCategoryRepository) bind CategoryRepository::class
    singleOf(::NetworkStatisticsRepository) bind StatisticsRepository::class
    singleOf(::NetworkSuggestionRepository) bind SuggestionRepository::class
//    singleOf(::FakeSuggestionRepository) bind SuggestionRepository::class
//    singleOf(::FakeTaskRepository) bind TaskRepository::class
    singleOf(::NetworkTaskRepository) bind TaskRepository::class
}
