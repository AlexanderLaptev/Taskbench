package cs.vsu.taskbench.domain.usecase

import cs.vsu.taskbench.data.PreloadRepository
import org.koin.dsl.module

val useCaseModule = module {
    single {
        val preloadRepos = listOf<PreloadRepository>()
        BootstrapUseCase(get(), preloadRepos)
    }
}
