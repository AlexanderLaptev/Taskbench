package cs.vsu.taskbench.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.FakeAuthService
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

private val Context.dataStore by preferencesDataStore("settings")

val dataModule = module {
    single { get<Context>().dataStore }
    single<AuthService> { FakeAuthService(get()) }
    single<UserRepository> { FakeUserRepository(get()) }
    single<CategoryRepository> { FakeCategoryRepository }
    single<StatisticsRepository> { FakeStatisticsRepository }
    single<SuggestionRepository> { FakeSuggestionRepository }
    single<TaskRepository> { FakeTaskRepository(get()) }
}
