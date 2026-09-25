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

@Composable
fun EncabezadoConductor(
    nombre: String = "Rafael",
    ubicacion: String = "Bosques de San Sebastian"
) {

    // Obtiene los colores dependiendo de la hora del día
    val coloresFondo = remember {
        obtenerColoresFondo()
    }

    // Gradiente del encabezado
    val fondoHorario = remember(coloresFondo) {
        Brush.horizontalGradient(coloresFondo)
    }

    val amarilloGps = Color(0xFFFFC107)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = fondoHorario,
                shape = RoundedCornerShape(
                    bottomStart = 14.dp,
                    bottomEnd = 14.dp
                )
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 18.dp,
                    bottom = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // INFORMACIÓN DEL CONDUCTOR
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = nombre,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = "GPS Activo",
                    color = amarilloGps,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = ubicacion,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 21.sp
                )
            }

            // AVATAR DEL CONDUCTOR
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color.White
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}