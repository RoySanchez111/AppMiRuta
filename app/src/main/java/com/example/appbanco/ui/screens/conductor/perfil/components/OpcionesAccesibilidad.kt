package com.example.appbanco.ui.screens.conductor.perfil.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appbanco.ui.theme.EscalaAccesibilidad
import com.example.appbanco.ui.viewmodel.MainViewModel

@Composable
fun OpcionesAccesibilidad(
    viewModel: MainViewModel? = null
) {
    val haptic = LocalHapticFeedback.current
    val escalaActual = viewModel?.escalaFuente?.collectAsState()?.value ?: EscalaAccesibilidad.MEDIANO
    val onSurfaceColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val currentDensity = LocalDensity.current

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // TÍTULO LIMPIO
        Text(
            text = "Tamaño de letra",
            color = onSurfaceColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // BARRA DE SELECCIÓN CON DENSIDAD FIJA PARA EVITAR DESBORDAMIENTO DE TEXTO AL ESCALAR
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = currentDensity.density,
                fontScale = 1.0f
            )
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    EscalaAccesibilidad.entries.forEach { nivel ->
                        val seleccionado = escalaActual == nivel

                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel?.cambiarEscalaFuente(nivel)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (seleccionado) primaryColor else Color.Transparent,
                            shadowElevation = if (seleccionado) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 2.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = nivel.simboloAa,
                                    fontSize = when (nivel) {
                                        EscalaAccesibilidad.PEQUEÑO -> 12.sp
                                        EscalaAccesibilidad.MEDIANO -> 14.sp
                                        EscalaAccesibilidad.GRANDE -> 16.sp
                                        EscalaAccesibilidad.EXTRA_GRANDE -> 18.sp
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (seleccionado) Color.White else primaryColor,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.width(3.dp))

                                Text(
                                    text = nivel.nombre.substringBefore(" "),
                                    fontSize = 11.sp,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                    color = if (seleccionado) Color.White else onSurfaceColor.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}