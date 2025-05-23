package cs.vsu.taskbench.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.StatisticsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TaskCreationScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TaskListScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.ramcosta.composedestinations.utils.startDestination
import cs.vsu.taskbench.R
import cs.vsu.taskbench.ui.theme.Black
import cs.vsu.taskbench.ui.theme.LightYellow
import cs.vsu.taskbench.ui.theme.TaskbenchTheme

private enum class TopLevelDestination(
    val direction: Direction,
    @DrawableRes val iconId: Int,
) {
    TaskCreation(TaskCreationScreenDestination, R.drawable.ic_add_circle_outline),
    TaskList(TaskListScreenDestination, R.drawable.ic_list),
    Statistics(StatisticsScreenDestination, R.drawable.ic_chart),
    Settings(SettingsScreenDestination, R.drawable.ic_gear),
}

@Composable
fun NavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    onReset: () -> Boolean = { true },
) {
    val currentDestination = navController
        .currentDestinationAsState()
        .value ?: NavGraphs.root.startDestination
    val navigator = navController.rememberDestinationsNavigator()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(LightYellow)
            .padding(top = 4.dp, bottom = 4.dp)
            .navigationBarsPadding()
            .defaultMinSize(minHeight = 48.dp)
            .fillMaxWidth()
            .selectableGroup(),
    ) {
        for (destination in TopLevelDestination.entries) {
            NavigationBarItem(
                icon = painterResource(destination.iconId),
                selected = currentDestination == destination.direction,
                interactionSource = interactionSource,
                onClick = {
                    if (currentDestination == destination.direction) {
                        if (onReset()) return@NavigationBarItem
                    }

                    navigator.popBackStack()
                    navigator.navigate(destination.direction) {
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}

@Composable
private fun RowScope.NavigationBarItem(
    icon: Painter,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) {
    val color by animateColorAsState(if (selected) Black else Color(0xFF8D8D8D))
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .weight(1.0f),
    ) {
        val tintColor = color
        Icon(
            painter = icon,
            contentDescription = null,
            tint = tintColor,
            modifier = modifier.size(36.dp)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TaskbenchTheme {
        NavigationBar(rememberNavController())
    }
}
