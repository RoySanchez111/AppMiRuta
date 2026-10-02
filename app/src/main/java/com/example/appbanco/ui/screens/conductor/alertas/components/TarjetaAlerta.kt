package com.example.appbanco.ui.screens.conductor.alertas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appbanco.ui.screens.Incidencia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaAlerta(
    incidencia: Incidencia? = null,
    onAplicarDesvio: () -> Unit,
    onEliminar: () -> Unit
) {

    val haptic = LocalHapticFeedback.current

    // ==========================================
    // ESTADO DEL DESLIZAMIENTO
    // ==========================================
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { nuevoEstado ->

            when (nuevoEstado) {

                // Deslizamiento completo hacia la izquierda
                SwipeToDismissBoxValue.EndToStart -> {

                    haptic.performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )

                    onEliminar()

                    true
                }

                // Permite que regrese a su posición normal
                SwipeToDismissBoxValue.Settled -> true

                // No permitimos deslizar hacia la derecha
                else -> false
            }
        }
    )

    // ==========================================
    // DESLIZAR PARA ELIMINAR
    // ==========================================
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,

        // ======================================
        // FONDO ROJO
        // ======================================
        backgroundContent = {

            val colorFondo =
                if (
                    dismissState.dismissDirection ==
                    SwipeToDismissBoxValue.EndToStart
                ) {
                    Color(0xFFD71920)
                } else {
                    Color.Transparent
                }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = colorFondo,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Eliminar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar alerta",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) {

        // ==========================================
        // TARJETA DE ALERTA
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {

            Column(
                modifier = Modifier.padding(8.dp)
            ) {

                // ==================================
                // ENCABEZADO
                // ==================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = incidencia?.tipo ?: "Retraso Grave",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                color = Color(0xFFD71920),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            )
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = incidencia?.tiempo ?: "Hace 10 min",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                // INFORMACIÓN PRINCIPAL
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFFFFD9D9),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alerta",
                            tint = Color(0xFFD71920),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Column {

                        Text(
                            text = incidencia?.titulo ?: "Retraso grave –",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Text(
                            text = if (incidencia != null) "Ruta ${incidencia.ruta}" else "Ruta 15",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                // DESCRIPCIÓN
                Text(
                    text = incidencia?.descripcion ?: "Interrupción parcial del servicio en la Colonia Serdán. Retrasos de 10 a 30 min.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                // BOTÓN DE DESVÍO
                Button(
                    onClick = onAplicarDesvio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4)
                    )
                ) {

                    Text(
                        text = "Aplicar desvío sugerido",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}