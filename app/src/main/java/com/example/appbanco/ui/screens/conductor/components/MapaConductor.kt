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
import com.example.appbanco.ui.screens.generarParadasAdaptativas
import com.mapbox.geojson.Point

@Composable
fun MapaConductor(
    ubicacionActual: Point,
    onActualizar: () -> Unit = {},
    onCentrarUbicacion: () -> Unit = {},
    onAlerta: () -> Unit = {}
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    )
    {

        // mapa
        MapaOptimizadoContainer(
            modifier = Modifier.fillMaxSize(),
            paradas = generarParadasAdaptativas(ubicacionActual),
            conductores = emptyList(),
            centroPoint = ubicacionActual,
            ubicacionCentradaPoint = ubicacionActual,
            onParadaSelect = {}
        )

        // botones del lado derecho
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 14.dp,
                    bottom = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ACTUALIZAR
            Surface(
                onClick = onActualizar,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 5.dp,
                border = BorderStroke(
                    1.dp,
                    Color(0xFFE5E5E5)
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Actualizar ubicación",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // centrar gps
            Surface(
                onClick = onCentrarUbicacion,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE65100),
                shadowElevation = 6.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
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
                color = Color(0xFFC90000),
                shadowElevation = 7.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Enviar alerta",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}