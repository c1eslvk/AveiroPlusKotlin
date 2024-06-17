package com.example.aveiroplus.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.aveiroplus.R

@Composable
fun BottomNavigationBar(navController: NavController, userRole: String) {
    val items = if (userRole == "ADMIN") {
        listOf(
            BottomNavItem("admin", "Admin", ImageVector.vectorResource(id = R.drawable.ic_home)),
            BottomNavItem("profile", "Profile", ImageVector.vectorResource(id = R.drawable.ic_profile))
        )
    } else {
        listOf(
            BottomNavItem("home", "Home", ImageVector.vectorResource(id = R.drawable.ic_home)),
            BottomNavItem("map", "Map", ImageVector.vectorResource(id = R.drawable.ic_map)),
            BottomNavItem("profile", "Profile", ImageVector.vectorResource(id = R.drawable.ic_profile))
        )
    }

    var selectedItem by remember { mutableStateOf(items.first().route) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = isSelected,
                label = {
                    Text(
                        item.title,
                    )
                },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.title,
                    )
               },
                onClick = {
                    selectedItem = item.route
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemColors(
                    selectedIconColor = MaterialTheme.colorScheme.tertiary,
                    selectedTextColor = MaterialTheme.colorScheme.tertiary,
                    selectedIndicatorColor = MaterialTheme.colorScheme.background,
                    unselectedIconColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                    unselectedTextColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                    disabledIconColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                    disabledTextColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                )
            )
        }
    }
}

data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)
