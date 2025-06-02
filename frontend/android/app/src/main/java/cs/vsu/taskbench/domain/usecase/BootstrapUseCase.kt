package cs.vsu.taskbench.domain.usecase

import android.content.Context
import android.util.Log
import cs.vsu.taskbench.data.PreloadRepository
import cs.vsu.taskbench.data.analytics.AnalyticsFacade
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.withAuth
import cs.vsu.taskbench.ui.util.hasInternetConnection
import cs.vsu.taskbench.util.HttpStatusCodes
import cs.vsu.taskbench.util.MockRandom
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

class BootstrapUseCase(
    private val context: Context,
    private val authService: AuthService,
    private val preloadRepos: List<PreloadRepository>,
) {
    companion object {
        private val TAG = BootstrapUseCase::class.simpleName
    }

    sealed interface Result {
        data object Success : Result
        data object NoInternet : Result
        data object CouldNotConnect : Result
        data object LoginRequired : Result
        data class UnknownError(val error: Exception) : Result
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
                authService.withAuth { repo.preload() }
            }
        } catch (e: HttpException) {
            if (e.code() == HttpStatusCodes.UNAUTHORIZED) {
                Log.d(TAG, "invoke: bootstrap failed, authorization required")
                AnalyticsFacade.logError("bootstrap", e)
                return Result.LoginRequired
            }

            Log.d(TAG, "invoke: bootstrap failed, HTTP error")
            Log.d(TAG, "invoke: code=${e.code()}")
            Log.d(TAG, "invoke: errorBody=${e.response()?.errorBody()?.string()}")
            AnalyticsFacade.logError("bootstrap", e)
            return Result.UnknownError(e)
        } catch (e: Exception) {
            when (e) {
                is ConnectException, is SocketTimeoutException -> {
                    Log.e(TAG, "invoke: bootstrap failed, could not connect to the server")
                    AnalyticsFacade.logError("bootstrap", e)
                    return Result.CouldNotConnect
                }
            }

            Log.e(TAG, "invoke: bootstrap failed because of an unknown exception", e)
            AnalyticsFacade.logError("bootstrap", e)
            return Result.UnknownError(e)
        }

        Log.d(TAG, "invoke: bootstrap success")
        return Result.Success
    }
}
