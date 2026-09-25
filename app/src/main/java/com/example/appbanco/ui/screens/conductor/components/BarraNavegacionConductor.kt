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

    val coral = Color(0xFFF26767)
    val gris = Color(0xFF555555)

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        containerColor = Color.White,
        tonalElevation = 2.dp
    ) {

        // Inicio
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Inicio"
                )
            },
            label = {
                Text(
                    text = "Inicio",
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = coral,
                selectedTextColor = coral,
                indicatorColor = Color.Transparent,
                unselectedIconColor = gris,
                unselectedTextColor = gris
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
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Alertas"
                )
            },
            label = {
                Text(
                    text = "Alertas",
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = coral,
                selectedTextColor = coral,
                indicatorColor = Color.Transparent,
                unselectedIconColor = gris,
                unselectedTextColor = gris
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
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Cuenta"
                )
            },
            label = {
                Text(
                    text = "Cuenta",
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = coral,
                selectedTextColor = coral,
                indicatorColor = Color.Transparent,
                unselectedIconColor = gris,
                unselectedTextColor = gris
            )
        )
    }
}