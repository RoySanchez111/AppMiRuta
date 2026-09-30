package com.example.appbanco.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appbanco.ui.components.BarraNavegacionInferior
import com.example.appbanco.ui.components.RielNavegacionLateral
import com.example.appbanco.ui.components.EncabezadoGlobal
import com.example.appbanco.logic.obtenerMensajeBienvenida
import com.example.appbanco.ui.components.TimeBasedBackground
import com.example.appbanco.ui.screens.*
import com.example.appbanco.tutorial.PantallaTutorial
import com.example.appbanco.ui.screens.conductor.PantallaInicioConductor
import com.example.appbanco.ui.screens.conductor.alertas.PantallaAlertasConductor
import com.example.appbanco.ui.viewmodel.MainViewModel
import com.example.appbanco.ui.viewmodel.LoginViewModel
import com.example.appbanco.data.database.AppDatabase
import com.example.appbanco.logic.SessionManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appbanco.ui.screens.conductor.perfil.PantallaPerfilConductor
import com.example.appbanco.ui.screens.conductor.configuracion.PantallaConfiguracionConductor
import com.example.appbanco.ui.screens.conductor.privacidad.PantallaPrivacidadConductor
import com.example.appbanco.ui.screens.conductor.ayuda.PantallaAyudaConductor
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun NavegacionMiRuta(
    viewModel: MainViewModel,
    database: AppDatabase,
    sessionManager: SessionManager
) {
    val startDest by viewModel.startDestination.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route
    val esPantallaConductor =
        rutaActual == "inicio_conductor" ||
                rutaActual == "alertas_conductor" ||
                rutaActual == "cuenta_conductor" ||
                rutaActual == "configuracion_conductor" ||
                rutaActual == "privacidad_conductor" ||
                rutaActual == "ayuda_conductor"

    val esPantallaApp =
        rutaActual != "splash" &&
                rutaActual != "login" &&
                rutaActual != "loading" &&
                rutaActual != "registro" &&
                rutaActual != "tutorial" &&
                !esPantallaConductor
    val usuarioNombre by viewModel.usuarioActual.collectAsState()
    val fotoPerfilUri by viewModel.fotoPerfilUri.collectAsState()
    val mensajeBienvenida = obtenerMensajeBienvenida(usuarioNombre)
    val tituloHeader = when (rutaActual) {
        "principal" -> if (mensajeBienvenida.contains(",")) mensajeBienvenida.split(",")[1].trim() else usuarioNombre
        "horario" -> "Planea tu viaje"
        "alertas" -> "Alertas importantes"
        "cuenta" -> "Perfil"
        "configuracion" -> "Ajustes"
        "privacidad" -> "Privacidad y seguridad"
        "ayuda" -> "Ayuda y Soporte"
        else -> ""
    }
    val subtituloHeader = if (rutaActual == "principal") {
        if (mensajeBienvenida.contains(",")) mensajeBienvenida.split(",")[0] + "," else "Hola,"
    } else null

    val configuration = LocalConfiguration.current
    val esPantallaAncha = configuration.screenWidthDp >= 600

    val userRole by sessionManager.userRole.collectAsState(initial = "pasajero")
    val esConductor = userRole == "conductor" || userRole == "admin"

    Row(modifier = Modifier.fillMaxSize()) {
        if (esPantallaApp && esPantallaAncha) {
            RielNavegacionLateral(navController, rutaActual, esConductor)
        }

        Scaffold(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                if (esPantallaApp) {
                    EncabezadoGlobal(
                        titulo = tituloHeader,
                        subtitulo = subtituloHeader,
                        fotoUri = fotoPerfilUri,
                        inicialUsuario = if (usuarioNombre.isNotBlank()) usuarioNombre.take(1)
                            .uppercase() else "U",
                        onBackClick = if (rutaActual != "principal" && rutaActual != "inicio_conductor") {
                            {
                                val destinoBack =
                                    if (esConductor) "inicio_conductor" else "principal"
                                if (navController.previousBackStackEntry != null) {
                                    navController.popBackStack()
                                } else {
                                    navController.navigate(destinoBack) {
                                        popUpTo(destinoBack) { inclusive = true }
                                    }
                                }
                            }
                        } else null,
                        onProfileClick = {
                            if (rutaActual != "cuenta" && rutaActual != "cuenta_conductor") {
                                val destinoCuenta = if (esConductor) "cuenta_conductor" else "cuenta"
                                val destinoInicio = if (esConductor) "inicio_conductor" else "principal"
                                navController.navigate(destinoCuenta) {
                                    popUpTo(destinoInicio) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (esPantallaApp && !esPantallaAncha) {
                    BarraNavegacionInferior(navController, rutaActual, esConductor)
                }
            }
        ) { paddingValues ->
            val modifier =
                if (esPantallaApp) Modifier.padding(paddingValues) else Modifier.fillMaxSize()

            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = modifier
            ) {
                composable("splash") { TimeBasedBackground { PantallaSplash(navController) } }
                composable("loading") {
                    TimeBasedBackground {
                        PantallaLoading(
                            navController,
                            startDest,
                            sessionManager
                        )
                    }
                }
                composable("tutorial") {
                    TimeBasedBackground {
                        PantallaTutorial(
                            navController,
                            sessionManager
                        )
                    }
                }
                composable("login") {
                    val loginViewModel: LoginViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                                    @Suppress("UNCHECKED_CAST")
                                    return LoginViewModel(database.userDao(), sessionManager) as T
                                }
                                throw IllegalArgumentException("Clase ViewModel no conocida: ${modelClass.name}")
                            }
                        }
                    )
                    TimeBasedBackground {
                        PantallaLogin(
                            navController,
                            loginViewModel,
                            sessionManager
                        )
                    }
                }
                composable("registro") {
                    TimeBasedBackground {
                        PantallaRegistro(navController, database.userDao(), sessionManager)
                    }
                }
                composable("principal") {
                    PantallaPrincipal(navController)
                }
                composable("inicio_conductor") {
                    PantallaInicioConductor(navController)
                }
                composable("alertas_conductor") {
                    PantallaAlertasConductor(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
                composable("horario") {
                    PantallaHorarios(navController, viewModel)
                }
                composable("alertas") {
                    PantallaAlertas(navController, viewModel)
                }
                composable("cuenta") {
                    PantallaCuenta(
                        sessionManager,
                        navController,
                        viewModel,
                        database.userDao()
                    )
                }

                composable("configuracion") {
                    PantallaConfiguracion(
                        sessionManager,
                        navController,
                        viewModel
                    )
                }

                composable("privacidad") {
                    PantallaPrivacidad(navController)
                }

                composable("ayuda") {
                    PantallaAyudaYSoporte(navController)
                }
                composable("cuenta_conductor") {
                    PantallaPerfilConductor(navController, viewModel)
                }
                composable("configuracion_conductor") {
                    PantallaConfiguracionConductor(navController)
                }

                composable("privacidad_conductor") {
                    PantallaPrivacidadConductor(navController)
                }

                composable("ayuda_conductor") {
                    PantallaAyudaConductor(navController)
                }
            }
        }
    }
}


