package com.example.appbanco.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appbanco.logic.obtenerLogoSegunHora
import com.example.appbanco.ui.components.MinimalistAnimation
import com.example.appbanco.ui.components.TimeBasedBackground
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PantallaSplash(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(2000.milliseconds)
        navController.navigate("loading") {
            popUpTo("splash") { inclusive = true }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = obtenerLogoSegunHora()),
            contentDescription = "Logo MiRuta",
            modifier = Modifier.size(240.dp)
        )
    }
}

@Composable
fun PantallaLoading(navController: NavController, targetDestination: String) {
    LaunchedEffect(Unit) {
        delay(3000.milliseconds)
        navController.navigate(targetDestination) {
            popUpTo("loading") { inclusive = true }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        MinimalistAnimation()
    }
}
