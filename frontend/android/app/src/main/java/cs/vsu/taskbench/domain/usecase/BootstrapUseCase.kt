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
        if (authTokenRepo.getSavedTokens() == null) return Result.LoginRequired

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
            Log.d(TAG, "bootstrapping repository $repo")
            val result = repo.preload()
            if (!result) {
                Log.d(TAG, "bootstrap failed, attempting token refresh")
                authTokenRepo.refreshTokens() ?: let {
                    Log.d(TAG, "bootstrap failed: refresh tokens failed")
                    return false
                }
                if (!repo.preload()) {
                    Log.d(TAG, "bootstrap failed: preload after refresh failed")
                    return false
                }
            }
            Log.d(TAG, "preloaded $repo successfully")
        }
        return true
    }
}
