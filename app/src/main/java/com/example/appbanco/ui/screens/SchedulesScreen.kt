package com.example.appbanco.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appbanco.logic.LineaHorario
import com.example.appbanco.logic.ServicioHorarios
import com.example.appbanco.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHorarios(navController: NavController, viewModel: MainViewModel) {
    val servicioHorarios = viewModel.servicioHorarios
    val onBackground = MaterialTheme.colorScheme.onBackground
    val onSurface = MaterialTheme.colorScheme.onSurface
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var busquedaTexto by remember { mutableStateOf("") }
    var fechaOffsetDias by remember { mutableStateOf(0) }
    var lineaSeleccionada by remember { mutableStateOf<LineaHorario?>(null) }

    val horaActualFormateada = remember(fechaOffsetDias) { servicioHorarios.obtenerHoraActualFormateada() }
    val lineasFiltradas by remember(busquedaTexto) { derivedStateOf { servicioHorarios.buscarLineas(busquedaTexto) } }
    val proximasSalidasPlazaMayor by remember(fechaOffsetDias) { derivedStateOf { servicioHorarios.calcularProximasSalidas(frecuenciaMinutos = 8, cantidad = 3, fechaOffsetDias = fechaOffsetDias) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = busquedaTexto,
            onValueChange = { busquedaTexto = it },
            placeholder = { Text("Buscar ruta por código o nombre...", color = onBackground.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = if (busquedaTexto.isNotEmpty()) {
                {
                    IconButton(onClick = { busquedaTexto = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda", tint = onBackground.copy(alpha = 0.5f))
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(onBackground.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
                .semantics {
                    contentDescription = "Campo de búsqueda de rutas e itinerarios"
                },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = onBackground.copy(alpha = 0.1f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = onBackground,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedTextColor = onBackground,
                focusedTextColor = onBackground
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtro de Fecha",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = onSurface
                    )
                    Text(
                        text = "Hora actual: $horaActualFormateada",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val opcionesFecha = listOf("Hoy", "Mañana", "En 2 días")
                    opcionesFecha.forEachIndexed { index, etiqueta ->
                        val esSeleccionado = fechaOffsetDias == index
                        Surface(
                            color = if (esSeleccionado) MaterialTheme.colorScheme.primary else onBackground.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    fechaOffsetDias = index
                                }
                        ) {
                            Text(
                                text = etiqueta,
                                color = if (esSeleccionado) Color.White else onSurface.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Próximas Salidas (Plaza Mayor)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = onSurface
                        )
                    }
                    Text("Frecuencia: 8 min", fontSize = 11.sp, color = onSurface.copy(alpha = 0.6f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    proximasSalidasPlazaMayor.forEachIndexed { index, hora ->
                        val esInmediata = index == 0 && fechaOffsetDias == 0
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = if (esInmediata) MaterialTheme.colorScheme.primary else onBackground.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (esInmediata) MaterialTheme.colorScheme.primary else onBackground.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (esInmediata) "Próximo" else "Salida ${index + 1}",
                                    fontSize = 10.sp,
                                    color = if (esInmediata) Color.White.copy(alpha = 0.9f) else onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = hora,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (esInmediata) Color.White else onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Líneas y Rutas Activas",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = onSurface,
            modifier = Modifier
                .align(Alignment.Start)
                .semantics { heading() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (lineasFiltradas.isEmpty()) {
            Text(
                text = "No se encontraron rutas con '$busquedaTexto'",
                color = onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            lineasFiltradas.forEach { linea ->
                key(linea.codigo) {
                    val salidasCalculadas = remember(fechaOffsetDias) {
                        servicioHorarios.calcularProximasSalidas(frecuenciaMinutos = linea.frecuenciaMinutos, cantidad = 3, fechaOffsetDias = fechaOffsetDias)
                    }
                    val proximaSalida = salidasCalculadas.firstOrNull() ?: "12:00"

                    TarjetaLineaHorario(
                        linea = linea,
                        proximaSalida = proximaSalida,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lineaSeleccionada = linea
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (lineaSeleccionada != null) {
        val linea = lineaSeleccionada!!
        val salidasCalculadas = remember(fechaOffsetDias, linea) {
            servicioHorarios.calcularProximasSalidas(frecuenciaMinutos = linea.frecuenciaMinutos, cantidad = 5, fechaOffsetDias = fechaOffsetDias)
        }

        DialogoDetalleLinea(
            linea = linea,
            salidasCalculadas = salidasCalculadas,
            onDismiss = { lineaSeleccionada = null }
        )
    }
}

@Composable
fun DialogoDetalleLinea(
    linea: LineaHorario,
    salidasCalculadas: List<String>,
    onDismiss: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(linea.color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(linea.codigo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(linea.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Recorrido: ${linea.recorrido}", fontSize = 12.sp, color = onSurface.copy(alpha = 0.6f))
                }
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Surface(
                    color = linea.color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Frecuencia de salida: Cada ${linea.frecuenciaMinutos} minutos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = linea.color,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Paradas de la Ruta:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                linea.paradas.forEach { parada ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = linea.color, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(parada, fontSize = 13.sp, color = onSurface.copy(alpha = 0.8f))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Próximas Salidas Estimadas:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    salidasCalculadas.take(3).forEach { hora ->
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = hora,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun TarjetaLineaHorario(linea: LineaHorario, proximaSalida: String, onClick: () -> Unit) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "Línea ${linea.codigo}, ${linea.nombre}, recorrido ${linea.recorrido}, frecuencia cada ${linea.frecuenciaMinutos} minutos, próxima salida a las $proximaSalida"
            }
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(linea.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = linea.codigo,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = linea.nombre,
                    color = onSurfaceColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = linea.recorrido,
                    color = onSurfaceColor.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Próx: $proximaSalida",
                    color = linea.color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cada ${linea.frecuenciaMinutos} min",
                    color = onSurfaceColor.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
