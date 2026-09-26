package com.example.appbanco.ui.screens.conductor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appbanco.ui.screens.MapaOptimizadoContainer
import com.mapbox.geojson.Point

@Composable
fun MapaConductor(
    ubicacionActual: Point,
    onActualizar: () -> Unit = {},
    onCentrarUbicacion: () -> Unit = {},
    onAlerta: () -> Unit = {}
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        // mapa
        MapaOptimizadoContainer(
            modifier = Modifier.fillMaxSize(),
            paradas = emptyList(),
            conductores = emptyList(),
            centroPoint = ubicacionActual,
            ubicacionCentradaPoint = ubicacionActual,
            onParadaSelect = {}
        )

        // botones del lado derecho
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ACTUALIZAR
            Surface(
                onClick = onActualizar,
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(16.dp),
                color = surfaceColor.copy(alpha = 0.95f),
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Actualizar ubicación",
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // centrar gps
            Surface(
                onClick = onCentrarUbicacion,
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(16.dp),
                color = primaryColor,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Centrar ubicación",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // boton de alertas
            Surface(
                onClick = onAlerta,
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Enviar alerta",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}
