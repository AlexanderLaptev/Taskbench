package cs.vsu.taskbench.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate


@Immutable
data class User(
    val id: Int,
    val email: String,
    val status: Status,
) {
    sealed interface Status {
        data object Unpaid : Status
        data class Premium(val activeUntil: LocalDate) : Status
    }
}
