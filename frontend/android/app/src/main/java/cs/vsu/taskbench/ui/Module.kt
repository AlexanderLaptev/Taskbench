package cs.vsu.taskbench.ui

import cs.vsu.taskbench.ui.component.dialog.edit.TaskEditDialogViewModel
import cs.vsu.taskbench.ui.list.TaskListScreenViewModel
import cs.vsu.taskbench.ui.login.LoginScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val uiModule = module {
    viewModel { LoginScreenViewModel(get(), get()) }
    viewModelOf(::TaskEditDialogViewModel)
    viewModelOf(::TaskListScreenViewModel)
}
