package cs.vsu.taskbench.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import cs.vsu.taskbench.data.auth.AuthorizationService
import cs.vsu.taskbench.data.auth.MockAuthorizationService
import cs.vsu.taskbench.data.user.FakeUserRepository
import cs.vsu.taskbench.data.user.UserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore("settings")

val dataModule = module {
    single { get<Context>().dataStore }
    single<UserRepository> { FakeUserRepository }
    single<AuthorizationService> { MockAuthorizationService }
    singleOf(::SettingsRepository)
}
