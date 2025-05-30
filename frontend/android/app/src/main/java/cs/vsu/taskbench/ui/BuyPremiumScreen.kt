package cs.vsu.taskbench.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import cs.vsu.taskbench.R
import cs.vsu.taskbench.data.analytics.AnalyticsFacade
import cs.vsu.taskbench.ui.component.Button
import cs.vsu.taskbench.ui.theme.AccentYellow
import cs.vsu.taskbench.ui.theme.DarkGray
import cs.vsu.taskbench.ui.theme.LightYellow
import cs.vsu.taskbench.ui.theme.TaskbenchTheme
import cs.vsu.taskbench.ui.theme.White


@Composable
@Destination<RootGraph>(style = ScreenTransitions::class)
fun BuyPremiumScreen(
    navigator: DestinationsNavigator,
    source: String = "unknown"
) {

    LaunchedEffect(Unit) {
        AnalyticsFacade.logPremiumScreenView(source)
    }

    Content(
        onBack = {
            navigator.navigateUp()
        },
        onBuy = {
            AnalyticsFacade.logSubscriptionButtonClick()
        }
    )
}

@Composable
fun Content(
    onBack: () -> Unit,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold { scaffoldPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp)
                .padding(scaffoldPadding),
        ) {
            Spacer(Modifier.height(64.dp))
            Image(
                painter = painterResource(R.drawable.logo_full_dark),
                contentDescription = null,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.text_stats_premium_part2))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                        append(stringResource(R.string.text_subscription_cost))
                    }
                    append(stringResource(R.string.text_buy_premium_last))
                },
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                modifier = Modifier
                    .background(color = LightYellow, shape = RoundedCornerShape(10.dp))
                    .padding(16.dp)
                    .fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Option(
                    text = stringResource(R.string.label_premium_advantage1),
                    icon = painterResource(R.drawable.img_task),
                )
                Option(
                    text = stringResource(R.string.label_premium_advantage2),
                    icon = painterResource(R.drawable.img_priority),
                )
                Option(
                    text = stringResource(R.string.label_premium_advantage3),
                    icon = painterResource(R.drawable.img_deadline),
                )
                Option(
                    text = stringResource(R.string.label_premium_advantage4),
                    icon = painterResource(R.drawable.img_category),
                )
            }
            Spacer(Modifier.height(16.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(color = AccentYellow, shape = RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.text_buy_premium_ready),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    text = stringResource(R.string.button_buy_subscription),
                    onClick = onBuy,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.label_premium_link_hint),
                    color = DarkGray,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        val buttonShape = RoundedCornerShape(100)
        Icon(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = stringResource(R.string.button_back),
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp)
                .padding(scaffoldPadding)
                .clip(buttonShape)
                .clickable(onClick = onBack)
                .background(color = White, shape = buttonShape)
                .padding(4.dp)
                .size(32.dp)
        )
    }
}

@Composable
private fun Option(
    text: String,
    icon: Painter,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(color = White, shape = RoundedCornerShape(10.dp))
            .padding(16.dp),
    ) {
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(42.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            textAlign = TextAlign.Left,
            fontSize = 16.sp,
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
private fun Preview() {
    TaskbenchTheme {
        Scaffold {
            Content(
                onBack = {},
                onBuy = {},
            )
        }
    }
}
