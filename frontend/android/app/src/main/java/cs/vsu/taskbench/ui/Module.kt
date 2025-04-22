package cs.vsu.taskbench.ui

import cs.vsu.taskbench.ui.login.LoginScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val uiModule = module {
    viewModelOf(::LoginScreenViewModel)
}
