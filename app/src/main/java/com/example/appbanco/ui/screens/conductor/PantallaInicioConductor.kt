package com.example.appbanco.ui.screens.conductor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.appbanco.ui.screens.conductor.components.*
import com.mapbox.geojson.Point

@Composable
fun PantallaInicioConductor(
    navController: NavController
) {

    // Datos temporales para probar la pantalla.
    // Después vendrán del conductor que inició sesión.
    val nombreConductor = "Rafael"
    val ubicacionTexto = "Bosques de San Sebastian"

    // Ubicación temporal.
    // Después utilizaremos el GPS real del conductor.
    var ubicacionActual by remember {
        mutableStateOf(
            Point.fromLngLat(
                -98.261833,
                18.999446
            )
        )
    }

    // Estado temporal del servicio.
    var enServicio by remember {
        mutableStateOf(true)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // 1. ENCABEZADO
        EncabezadoConductor(
            nombre = nombreConductor,
            ubicacion = ubicacionTexto
        )

        // 2. MAPA
        // Ocupa automáticamente todo el espacio disponible.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            MapaConductor(
                ubicacionActual = ubicacionActual,

                onActualizar = {
                    // Después conectaremos actualización GPS real.
                },

                onCentrarUbicacion = {
                    // Después centraremos el mapa en el GPS real.
                },

                onAlerta = {
                    navController.navigate("alertas")
                }
            )
        }

        // 3. ESTADO DEL CONDUCTOR
        EstadoServicio(
            enServicio = enServicio,
            onEstadoChange = { nuevoEstado ->
                enServicio = nuevoEstado
            }
        )

        // 4. NAVEGACIÓN INFERIOR
        BarraNavegacionConductor(
            navController = navController
        )
    }
}