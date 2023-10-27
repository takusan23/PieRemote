/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package io.github.takusan23.pieremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import io.github.takusan23.pieremote.ui.screen.HomeScreen
import io.github.takusan23.pieremote.ui.screen.QueueScreen
import io.github.takusan23.pieremote.ui.theme.PieRemoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

object Navigates {
    /** ホーム */
    const val HomeScreen = "home"

    /** キュー */
    const val QueueScreen = "queue"
}

@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()

    PieRemoteTheme {
        SwipeDismissableNavHost(navController = navController, startDestination = Navigates.HomeScreen) {
            composable(Navigates.HomeScreen) {
                HomeScreen(onClickQueue = { navController.navigate(Navigates.QueueScreen) })
            }
            composable(Navigates.QueueScreen) {
                QueueScreen()
            }
        }
    }
}
