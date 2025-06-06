package com.itza2k.privacyninja.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.itza2k.privacyninja.R
import com.itza2k.privacyninja.repository.PrivacyRepository
import com.itza2k.privacyninja.ui.screens.dashboard.DashboardScreen
import com.itza2k.privacyninja.ui.screens.network.NetworkNinjaScreen
import com.itza2k.privacyninja.ui.screens.permission.PermissionPatrolScreen
import com.itza2k.privacyninja.ui.screens.stealth.StealthModeScreen
import com.itza2k.privacyninja.ui.theme.NinjaAccent
import com.itza2k.privacyninja.ui.theme.TextSecondary

sealed class Screen(val route: String, val titleResId: Int, val icon: @Composable () -> Unit) {
    object Dashboard : Screen(
        route = "dashboard",
        titleResId = R.string.nav_dashboard,
        icon = { Icon(Icons.Filled.Dashboard, contentDescription = null, modifier = Modifier.size(28.dp)) }
    )

    object NetworkNinja : Screen(
        route = "network_ninja",
        titleResId = R.string.nav_network_ninja,
        icon = { Icon(Icons.Filled.Wifi, contentDescription = null, modifier = Modifier.size(28.dp)) }
    )

    object PermissionPatrol : Screen(
        route = "permission_patrol",
        titleResId = R.string.nav_permission_patrol,
        icon = { Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(28.dp)) }
    )

    object StealthMode : Screen(
        route = "stealth_mode",
        titleResId = R.string.nav_stealth_mode,
        icon = { Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(28.dp)) }
    )
}

val items = listOf(
    Screen.Dashboard,
    Screen.NetworkNinja,
    Screen.PermissionPatrol,
    Screen.StealthMode
)

@Composable
fun AppNavigation(privacyRepository: PrivacyRepository) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    privacyRepository = privacyRepository,
                    onNavigateToPermissionPatrol = {
                        navController.navigate(Screen.PermissionPatrol.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.NetworkNinja.route) {
                NetworkNinjaScreen(privacyRepository)
            }
            composable(Screen.PermissionPatrol.route) {
                PermissionPatrolScreen(privacyRepository)
            }
            composable(Screen.StealthMode.route) {
                StealthModeScreen(privacyRepository)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    Surface(
        modifier = Modifier
            .shadow(8.dp),
        color = Color(0xFF000000)
    ) {
        NavigationBar(
            modifier = Modifier.height(69.dp),
            containerColor = Color.Transparent,
            contentColor = TextSecondary
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val isSelected = { screen: Screen ->
                currentDestination?.hierarchy?.any { it.route == screen.route } == true
            }

            items.forEach { screen ->
                NavigationBarItem(
                    icon = screen.icon,
                    selected = isSelected(screen),
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NinjaAccent,
                        unselectedIconColor = TextSecondary.copy(alpha = 0.7f),
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }
    }
}
