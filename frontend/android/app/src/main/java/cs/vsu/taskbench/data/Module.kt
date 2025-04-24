package cs.vsu.taskbench.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import cs.vsu.taskbench.data.auth.AuthService
import cs.vsu.taskbench.data.auth.FakeAuthService
import cs.vsu.taskbench.data.user.FakeUserRepository
import cs.vsu.taskbench.data.user.UserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore("settings")

val dataModule = module {
    single { get<Context>().dataStore }
    single { FakeUserRepository(get()) } bind UserRepository::class
    singleOf(::FakeAuthService) bind AuthService::class
}
