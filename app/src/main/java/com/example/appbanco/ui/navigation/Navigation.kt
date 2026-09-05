package com.example.appbanco.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appbanco.ui.components.BarraNavegacionInferior
import com.example.appbanco.ui.components.EncabezadoGlobal
import com.example.appbanco.logic.obtenerMensajeBienvenida
import com.example.appbanco.ui.components.TimeBasedBackground
import com.example.appbanco.ui.screens.*

import com.example.appbanco.ui.viewmodel.MainViewModel
import com.example.appbanco.ui.viewmodel.LoginViewModel
import com.example.appbanco.data.database.AppDatabase
import com.example.appbanco.logic.SessionManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun NavegacionMiRuta(
    viewModel: MainViewModel,
    database: AppDatabase,
    sessionManager: SessionManager
) {
    val startDest by viewModel.startDestination
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route
    val esPantallaApp = rutaActual != "splash" && rutaActual != "login" && rutaActual != "loading" &&
            rutaActual != "registro"

    val mensajeBienvenida = obtenerMensajeBienvenida("Roy Sanchez")
    val tituloHeader = when (rutaActual) {
        "principal" -> mensajeBienvenida.split(",")[1].trim()
        "horario" -> "Planea tu viaje"
        "alertas" -> "Alertas importantes"
        "cuenta" -> "Perfil"
        else -> ""
    }
    val subtituloHeader = if (rutaActual == "principal") {
        mensajeBienvenida.split(",")[0] + ","
    } else null

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            if (esPantallaApp) {
                EncabezadoGlobal(
                    titulo = tituloHeader,
                    subtitulo = subtituloHeader,
                    onBackClick = if (rutaActual == "cuenta") {
                        { navController.popBackStack() }
                    } else null,
                    onProfileClick = {
                        if (rutaActual != "cuenta") {
                            navController.navigate("cuenta") {
                                popUpTo("principal") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        },
        bottomBar = { if (esPantallaApp) BarraNavegacionInferior(navController, rutaActual) }
    ) { paddingValues ->
        val modifier = if (esPantallaApp) Modifier.padding(paddingValues) else Modifier.fillMaxSize()

        NavHost(navController = navController, startDestination = "splash", modifier = modifier) {
            composable("splash") { TimeBasedBackground { PantallaSplash(navController) } }
            composable("loading") { TimeBasedBackground { PantallaLoading(navController, startDest) } }
            composable("login")
                                                                     {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return LoginViewModel(database.userDao(), sessionManager) as T
                        }
                    }
                )
                TimeBasedBackground { PantallaLogin(navController, loginViewModel) }
            }
            composable("registro") {
                TimeBasedBackground {
                    PantallaRegistro(navController)
                }
            }
            composable("principal") { PantallaPrincipal(navController) }
            composable("horario") { PantallaHorarios(navController) }
            composable("alertas") { PantallaAlertas(navController, viewModel) }
            composable("cuenta") { PantallaCuenta(sessionManager, navController) }
        }
    }
}