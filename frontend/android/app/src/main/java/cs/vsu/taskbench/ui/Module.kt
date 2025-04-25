package cs.vsu.taskbench.ui

import cs.vsu.taskbench.ui.create.TaskCreationScreen
import cs.vsu.taskbench.ui.create.TaskCreationScreenViewModel
import cs.vsu.taskbench.ui.login.LoginScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    viewModel { LoginScreenViewModel(get(), get()) }
    viewModel { TaskCreationScreenViewModel() }
}
