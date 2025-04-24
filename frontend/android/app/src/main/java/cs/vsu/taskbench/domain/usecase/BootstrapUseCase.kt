package cs.vsu.taskbench.domain.usecase

import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.data.auth.AuthTokenRepository

class BootstrapUseCase(
    private val authTokenRepo: AuthTokenRepository,
    private val preloadRepos: List<PreloadRepository>,
) {
    enum class Result {
        Success,
        LoginRequired,
    }

    suspend operator fun invoke(): Result {
        authTokenRepo.getSavedTokens() ?: return Result.LoginRequired
        return if (tryPreload()) Result.Success else Result.LoginRequired
    }

    private suspend fun tryPreload(): Boolean {
        for (repo in preloadRepos) {
            val result = repo.preload()
            if (!result) {
                authTokenRepo.refreshTokens() ?: return false
                if (!repo.preload()) return false
            }
        }
        return true
    }
}
