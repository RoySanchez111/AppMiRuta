package com.example.appbanco.ui.screens.conductor.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun BarraNavegacionConductor(
    navController: NavController
) {
    val themeColor = MaterialTheme.colorScheme.onSurface
    val containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    val primaryColor = MaterialTheme.colorScheme.primary

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        containerColor = containerColor,
        contentColor = themeColor
    ) {
        // Inicio
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
            label = { Text("Inicio", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = primaryColor,
                selectedTextColor = primaryColor,
                unselectedIconColor = themeColor.copy(alpha = 0.6f),
                unselectedTextColor = themeColor.copy(alpha = 0.6f),
                indicatorColor = primaryColor.copy(alpha = 0.15f)
            )
        )

        // Alertas
        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("alertas") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Outlined.Notifications, contentDescription = "Alertas") },
            label = { Text("Alertas", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = primaryColor,
                selectedTextColor = primaryColor,
                unselectedIconColor = themeColor.copy(alpha = 0.6f),
                unselectedTextColor = themeColor.copy(alpha = 0.6f),
                indicatorColor = primaryColor.copy(alpha = 0.15f)
            )
        )

        // Cuenta
        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("cuenta") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Cuenta") },
            label = { Text("Cuenta", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = primaryColor,
                selectedTextColor = primaryColor,
                unselectedIconColor = themeColor.copy(alpha = 0.6f),
                unselectedTextColor = themeColor.copy(alpha = 0.6f),
                indicatorColor = primaryColor.copy(alpha = 0.15f)
            )
        )
    }
}
