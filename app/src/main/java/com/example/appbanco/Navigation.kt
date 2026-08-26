package com.example.appbanco

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appbanco.ui.components.TimeBasedBackground

@Composable
fun NavegacionMiRuta() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val esPantallaApp = rutaActual != "splash" && rutaActual != "login" && rutaActual != "loading"

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            if (esPantallaApp && rutaActual != "cuenta") {
                EncabezadoUsuario()
            }
        },
        bottomBar = {
            if (esPantallaApp) {
                BarraNavegacionInferior(navController, rutaActual)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("splash") { TimeBasedBackground { PantallaSplash(navController) } }
            composable("loading") { TimeBasedBackground { PantallaLoading(navController) } }
            composable("login") { TimeBasedBackground { PantallaLogin(navController) } }
            composable("principal") { PantallaPrincipal(navController) }
            composable("horario") { PantallaHorarios(navController) }
            composable("alertas") { PantallaGenerica("Alertas", Icons.Default.Notifications) }
            composable("cuenta") { PantallaCuenta() }
        }
    }
}

@Composable
fun EncabezadoUsuario() {
    val saludo = obtenerMensajeBienvenida("Roy Sanchez")
    TimeBasedBackground(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = saludo.split(",")[0] + ",",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = saludo.split(",")[1].trim(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun BarraNavegacionInferior(navController: NavController, rutaActual: String?) {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.85f),
        contentColor = Color(0xFF282869)
    ) {
        val items = listOf(
            Triple("principal", "Inicio", Icons.Default.Home),
            Triple("horario", "Horario", Icons.Default.Schedule),
            Triple("alertas", "Alertas", Icons.Default.Notifications),
            Triple("cuenta", "Cuenta", Icons.Default.Person)
        )

        items.forEach { (ruta, etiqueta, icono) ->
            NavigationBarItem(
                icon = { Icon(icono, contentDescription = etiqueta) },
                label = { Text(etiqueta) },
                selected = rutaActual == ruta,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFF4556D),
                    selectedTextColor = Color(0xFFF4556D),
                    unselectedIconColor = Color(0xFF282869),
                    unselectedTextColor = Color(0xFF282869),
                    indicatorColor = Color(0xFF00BCD4).copy(alpha = 0.2f)
                ),
                onClick = {
                    if (rutaActual != ruta) {
                        navController.navigate(ruta) {
                            popUpTo("principal") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}