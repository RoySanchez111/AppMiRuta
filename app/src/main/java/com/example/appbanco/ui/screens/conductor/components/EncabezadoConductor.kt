package com.example.appbanco.ui.screens.conductor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appbanco.ui.components.obtenerColoresFondo
import java.util.Calendar

@Composable
fun EncabezadoConductor(
    nombre: String = "Rafita",
    ubicacion: String = "Puebla - Ruta Troncal Activa"
) {

    // ==========================================
    // COLORES SEGÚN LA HORA
    // ==========================================
    val coloresFondo = remember {
        obtenerColoresFondo()
    }

    val fondoHorario = remember(coloresFondo) {
        Brush.horizontalGradient(coloresFondo)
    }

    val amarilloGps = Color(0xFFFFC107)

    // ==========================================
    // SALUDO SEGÚN LA HORA
    // ==========================================
    val saludo = remember {
        val hora = Calendar.getInstance()
            .get(Calendar.HOUR_OF_DAY)

        when (hora) {
            in 6..11 -> "Buenos días"
            in 12..18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }

    // ==========================================
    // ENCABEZADO
    // ==========================================
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(
                brush = fondoHorario,
                shape = RoundedCornerShape(
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp
                )
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp)
                .padding(
                    start = 22.dp,
                    end = 22.dp,
                    top = 22.dp,
                    bottom = 22.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ======================================
            // INFORMACIÓN DEL CONDUCTOR
            // ======================================
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {

                // SALUDO
                Text(
                    text = "$saludo,",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                // NOMBRE
                Text(
                    text = nombre,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 31.sp
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                // GPS ACTIVO
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        modifier = Modifier.size(9.dp),
                        shape = CircleShape,
                        color = amarilloGps
                    ) {}

                    Spacer(
                        modifier = Modifier.width(7.dp)
                    )

                    Text(
                        text = "GPS Activo",
                        color = amarilloGps,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                // UBICACIÓN / RUTA
                Text(
                    text = ubicacion,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
            }

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            // ======================================
            // AVATAR
            // ======================================
            Surface(
                modifier = Modifier.size(68.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = nombre
                            .firstOrNull()
                            ?.uppercase()
                            ?: "C",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}