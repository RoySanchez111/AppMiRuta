package com.example.appbanco.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appbanco.logic.SessionManager
import com.example.appbanco.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun PantallaConfiguracion(
    sessionManager: SessionManager,
    navController: NavController,
    viewModel: MainViewModel
) {
    var notifTiempoReal by remember { mutableStateOf(true) }
    var notifRetrasos by remember { mutableStateOf(true) }
    var notifSonidoVibracion by remember { mutableStateOf(false) }

    var modoOffline by remember { mutableStateOf(true) }
    var unidadDistancia by remember { mutableStateOf("km") }

    val temaActual = viewModel.modoTema.value
    var temaSeleccionado by remember(temaActual) {
        mutableStateOf(
            when (temaActual) {
                "Claro" -> "Claro"
                "Oscuro" -> "Obscuro"
                else -> "Auto"
            }
        )
    }

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(surfaceColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Notificaciones",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = onSurfaceColor.copy(alpha = 0.8f),
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notificaciones en tiempo real", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onSurfaceColor)
                        Text("Alertas en tiempo real", fontSize = 12.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = notifTiempoReal,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            notifTiempoReal = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                    )
                }

                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.08f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Retraso de mis líneas", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onSurfaceColor)
                        Text("Alertas de incidentes en sus líneas", fontSize = 12.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = notifRetrasos,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            notifRetrasos = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                    )
                }

                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.08f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sonido y vibración", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onSurfaceColor)
                        Text("Solo alertas graves", fontSize = 12.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = notifSonidoVibracion,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            notifSonidoVibracion = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Viajes",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = onSurfaceColor.copy(alpha = 0.8f),
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Modo Offline", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onSurfaceColor)
                        Text("Sin datos", fontSize = 12.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = modoOffline,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            modoOffline = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                    )
                }

                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.08f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rutas descargadas", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onSurfaceColor)
                        Text("24 MB", fontSize = 12.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    }
                    TextButton(
                        onClick = {
                            Toast.makeText(context, "Mapas y rutas actualizados", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Actualizar", color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.08f))

                Column {
                    Text("Unidad de distancia", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onSurfaceColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = onSurfaceColor.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("km", "m", "millas").forEach { un ->
                                val esSel = unidadDistancia == un
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (esSel) primaryColor else Color.Transparent)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            unidadDistancia = un
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = un,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (esSel) Color.White else onSurfaceColor.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Apariencia",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = onSurfaceColor.copy(alpha = 0.8f),
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(14.dp)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = onSurfaceColor.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Claro", "Obscuro", "Auto").forEach { op ->
                            val esSel = temaSeleccionado == op
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (esSel) primaryColor else Color.Transparent)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        temaSeleccionado = op
                                        val nuevoTema = when (op) {
                                            "Claro" -> "Claro"
                                            "Obscuro" -> "Oscuro"
                                            else -> "Degradados"
                                        }
                                        viewModel.cambiarTema(nuevoTema)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = op,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (esSel) Color.White else onSurfaceColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    sessionManager.logout()
                    navController.navigate("login") {
                        popUpTo("principal") { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B), contentColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Cerrar Sesión", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
