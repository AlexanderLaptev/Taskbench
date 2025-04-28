package cs.vsu.taskbench.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.NetworkAuthService
import cs.vsu.taskbench.data.auth.NetworkAuthenticator
import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.data.category.FakeCategoryRepository
import cs.vsu.taskbench.data.statistics.FakeStatisticsRepository
import cs.vsu.taskbench.data.statistics.StatisticsRepository
import cs.vsu.taskbench.data.task.FakeSuggestionRepository
import cs.vsu.taskbench.data.task.FakeTaskRepository
import cs.vsu.taskbench.data.task.SuggestionRepository
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.data.user.FakeUserRepository
import cs.vsu.taskbench.data.user.UserRepository
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private val Context.dataStore by preferencesDataStore("settings")

val dataModule = module {
    single { get<Context>().dataStore }
    single { Moshi.Builder().build() }
    single {
        Retrofit.Builder()
            .baseUrl("http://193.135.137.154:8000/")
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    single<NetworkAuthenticator> { get<Retrofit>().create(NetworkAuthenticator::class.java) }
    single<AuthService> { NetworkAuthService(get(), get()) }
    single<UserRepository> { FakeUserRepository(get()) }
    single<CategoryRepository> { FakeCategoryRepository }
    single<StatisticsRepository> { FakeStatisticsRepository }
    single<SuggestionRepository> { FakeSuggestionRepository }
    single<TaskRepository> { FakeTaskRepository(get()) }
}
