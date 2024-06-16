package com.example.aveiroplus.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
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

    val colorScheme = MaterialTheme.colorScheme

    NavigationBar(
        containerColor = colorScheme.primary, // Use theme color
        contentColor = colorScheme.onPrimary // Use theme color
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = false,
                label = { Text(item.title) },
                icon = { Icon(item.icon, contentDescription = item.title) },
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorScheme.onPrimary,
                    unselectedIconColor = colorScheme.onSurface,
                    selectedTextColor = colorScheme.onPrimary,
                    unselectedTextColor = colorScheme.onSurface
                )
            )
        }
    }
}

data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)
