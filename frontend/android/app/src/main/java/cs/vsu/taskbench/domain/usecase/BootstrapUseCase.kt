package cs.vsu.taskbench.domain.usecase

import android.content.Context
import android.util.Log
import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.UnauthorizedException
import cs.vsu.taskbench.ui.util.hasInternetConnection
import cs.vsu.taskbench.util.HttpStatusCodes
import cs.vsu.taskbench.util.MockRandom
import retrofit2.HttpException

class BootstrapUseCase(
    private val context: Context,
    private val authService: AuthService,
    private val preloadRepos: List<PreloadRepository>,
) {
    companion object {
        private val TAG = BootstrapUseCase::class.simpleName
    }

    enum class Result {
        Success,
        LoginRequired,
        NoInternet,
    }

    suspend operator fun invoke(): Result {
        MockRandom.reset()

        if (!context.hasInternetConnection()) {
            Log.d(TAG, "invoke: no internet, returning early")
            return Result.NoInternet
        }

        Log.d(TAG, "invoke: bootstrap started")
        try {
            authService.getSavedTokens()
            for (repo in preloadRepos) {
                val repoName = repo::class.simpleName
                Log.d(TAG, "invoke: preloading $repoName")
                try {
                    repo.preload()
                } catch (e: UnauthorizedException) {
                    Log.d(TAG, "invoke: preloading $repoName failed, attempting token refresh")
                    authService.refreshTokens()
                    repo.preload()
                }
            }
        } catch (e: HttpException) {
            if (e.code() == HttpStatusCodes.UNAUTHORIZED) throw UnauthorizedException()
            Log.d(TAG, "invoke: bootstrap failed, HTTP error")
            Log.d(TAG, "invoke: code=${e.code()}")
            Log.d(TAG, "invoke: errorBody=${e.response()?.errorBody()?.string()}")
            throw e
        } catch (e: UnauthorizedException) {
            Log.d(TAG, "invoke: bootstrap failed, authorization required")
            return Result.LoginRequired
        } catch (e: Exception) {
            Log.e(TAG, "invoke: bootstrap failed because of an unknown exception", e)
            throw e
        }
        Log.d(TAG, "invoke: bootstrap success")
        return Result.Success
    }
}
