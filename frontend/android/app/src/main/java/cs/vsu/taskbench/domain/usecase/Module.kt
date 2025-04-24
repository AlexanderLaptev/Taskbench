package cs.vsu.taskbench.domain.usecase

import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.data.user.UserRepository
import org.koin.dsl.module

val useCaseModule = module {
    single {
        val preloadRepos = listOf<PreloadRepository>(get<UserRepository>())
        BootstrapUseCase(get(), preloadRepos)
    }
}
