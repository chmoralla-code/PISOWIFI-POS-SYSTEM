package com.pisowifi.pos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pisowifi.pos.PisoWifiApp
import com.pisowifi.pos.ui.screen.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Pisowifi : Screen("pisowifi")
    object AddPisowifi : Screen("pisowifi/add?deviceId={deviceId}") {
        fun createRoute(deviceId: Int? = null) =
            if (deviceId != null) "pisowifi/add?deviceId=$deviceId" else "pisowifi/add"
    }
    object AddSale : Screen("sale/add?pisowifiId={pisowifiId}") {
        fun createRoute(pisowifiId: Int? = null) =
            if (pisowifiId != null) "sale/add?pisowifiId=$pisowifiId" else "sale/add"
    }
    object Analytics : Screen("analytics")
    object Areas : Screen("areas")
    object Harvest : Screen("harvest")
}

@Composable
fun PosNavGraph() {
    val context = LocalContext.current
    val app = context.applicationContext as PisoWifiApp
    val repository = app.repository
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(repository = repository, navController = navController)
        }
        composable(Screen.Pisowifi.route) {
            PisowifiListScreen(repository = repository, navController = navController)
        }
        composable(
            route = Screen.AddPisowifi.route,
            arguments = listOf(navArgument("deviceId") {
                type = NavType.IntType; defaultValue = -1
            })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getInt("deviceId") ?: -1
            AddEditPisowifiScreen(
                repository = repository,
                navController = navController,
                editDeviceId = if (deviceId > 0) deviceId else null
            )
        }
        composable(
            route = Screen.AddSale.route,
            arguments = listOf(navArgument("pisowifiId") {
                type = NavType.IntType; defaultValue = -1
            })
        ) { backStackEntry ->
            val pisowifiId = backStackEntry.arguments?.getInt("pisowifiId") ?: -1
            AddSaleScreen(
                repository = repository,
                navController = navController,
                preselectedPisowifiId = if (pisowifiId > 0) pisowifiId else null
            )
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen(repository = repository, navController = navController)
        }
        composable(Screen.Areas.route) {
            AreasScreen(repository = repository, navController = navController)
        }
        composable(Screen.Harvest.route) {
            HarvestScreen(repository = repository, navController = navController)
        }
    }
}
