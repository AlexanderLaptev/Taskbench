package cs.vsu.taskbench.domain.usecase

import cs.vsu.taskbench.data.category.CategoryRepository
import cs.vsu.taskbench.data.statistics.StatisticsRepository
import cs.vsu.taskbench.data.subscription.SubscriptionManager
import cs.vsu.taskbench.data.task.FakeTaskRepository
import org.koin.dsl.module

val useCaseModule = module {
    single {
        val preloadRepositories = mutableListOf(
            get<CategoryRepository>(),
            get<StatisticsRepository>(),
            get<SubscriptionManager>(),
        )
        val fakeTaskRepo = getOrNull<FakeTaskRepository>()
        if (fakeTaskRepo != null) preloadRepositories += fakeTaskRepo

        BootstrapUseCase(get(), get(), preloadRepositories)
    }
}
