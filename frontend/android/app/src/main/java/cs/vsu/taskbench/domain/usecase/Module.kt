package cs.vsu.taskbench.domain.usecase

import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.data.statistics.StatisticsRepository
import cs.vsu.taskbench.data.task.FakeTaskRepository
import cs.vsu.taskbench.data.user.UserRepository
import org.koin.dsl.module

val useCaseModule = module {
    single {
        val preloadRepositories = mutableListOf(
            get<UserRepository>(),
            get<CategoryRepository>(),
            get<StatisticsRepository>(),
        )
        val fakeTaskRepo = getOrNull<FakeTaskRepository>()
        if (fakeTaskRepo != null) preloadRepositories += fakeTaskRepo

        BootstrapUseCase(get(), get(), preloadRepositories)
    }
}
