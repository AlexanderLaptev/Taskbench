package cs.vsu.taskbench.domain.usecase

import android.util.Log
import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.data.auth.AuthService

class BootstrapUseCase(
    private val authTokenRepo: AuthService,
    private val preloadRepos: List<PreloadRepository>,
) {
    companion object {
        private val TAG = BootstrapUseCase::class.simpleName
    }

    enum class Result {
        Success,
        LoginRequired,
    }

    suspend operator fun invoke(): Result {
        if (authTokenRepo.getSavedTokens() == null) {
            Log.d(TAG, "no saved tokens")
            return Result.LoginRequired
        }

        return if (tryPreload()) {
            Log.d(TAG, "bootstrap success!")
            Result.Success
        } else {
            Log.d(TAG, "preload failed")
            Result.LoginRequired
        }
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
