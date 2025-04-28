package cs.vsu.taskbench.domain.usecase

import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.data.statistics.StatisticsRepository
import cs.vsu.taskbench.data.task.TaskRepository
import cs.vsu.taskbench.data.user.UserRepository
import org.koin.dsl.module

val useCaseModule = module {
    single {
        val preloadRepositories = listOf(
            get<UserRepository>(),
            get<CategoryRepository>(),
            get<StatisticsRepository>(),
            get<TaskRepository>(),
        )
        BootstrapUseCase(get(), preloadRepositories)
    }
}
