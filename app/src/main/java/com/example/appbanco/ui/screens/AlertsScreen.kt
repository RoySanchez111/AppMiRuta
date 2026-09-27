package com.example.appbanco.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appbanco.ui.viewmodel.MainViewModel
import androidx.constraintlayout.compose.*
import com.example.appbanco.logic.reproducirSonidoNotificacion
import kotlinx.coroutines.delay
import java.util.UUID

data class Incidencia(
    val id: String = UUID.randomUUID().toString(),
    val tipo: String,
    val ruta: String,
    val titulo: String,
    val descripcion: String,
    val tiempo: String,
    val colorEtiqueta: Color,
    val colorRuta: Color,
    var confirmaciones: Int = 1,
    var esGlobal: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAlertas(navController: NavController, viewModel: MainViewModel) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val listaIncidencias = viewModel.listaIncidencias
    var mostrarDialogo by remember { mutableStateOf(false) }
    var mostrarExito by remember { mutableStateOf(false) }

    val alertasGlobales = listaIncidencias.filter { it.esGlobal }
    val alertasPendientes = listaIncidencias.filter { !it.esGlobal }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 850.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (alertasGlobales.isEmpty() && alertasPendientes.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                        Surface(
                            color = surfaceColor,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2ECC71), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("¡No hay incidencias reportadas!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurface)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("El servicio de transporte opera con normalidad.", fontSize = 13.sp, color = onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "👈 Desliza una alerta a la izquierda para eliminarla",
                            fontSize = 12.sp,
                            color = onSurface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .semantics { contentDescription = "Pista: Desliza cualquier tarjeta a la izquierda para eliminar la alerta" }
                        )
                    }
                }

                if (alertasGlobales.isNotEmpty()) {
                    item {
                        Text(
                            text = "🟢 Alertas Verificadas y Globales",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF2ECC71),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .semantics { heading() }
                        )
                    }
                    items(items = alertasGlobales, key = { it.id }) { incidencia ->
                        ItemAlertaDeslizable(
                            incidencia = incidencia,
                            onEliminar = {
                                listaIncidencias.remove(incidencia)
                                Toast.makeText(context, "Alerta en ruta ${incidencia.ruta} eliminada", Toast.LENGTH_SHORT).show()
                            },
                            onConfirmar = {}
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                if (alertasPendientes.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "📍 Reportes Cercanos por Verificar (Pendientes)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFF39C12),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .semantics { heading() }
                        )
                    }
                    items(items = alertasPendientes, key = { it.id }) { incidencia ->
                        ItemAlertaDeslizable(
                            incidencia = incidencia,
                            onEliminar = {
                                listaIncidencias.remove(incidencia)
                                Toast.makeText(context, "Reporte cercano descartado", Toast.LENGTH_SHORT).show()
                            },
                            onConfirmar = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.confirmarIncidencia(incidencia.id)
                                Toast.makeText(context, "✅ ¡Alerta confirmada! Promovida a global para todos", Toast.LENGTH_LONG).show()
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .height(56.dp)
                    .width(280.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp))
                    .semantics(mergeDescendants = true) {
                        role = Role.Button
                        contentDescription = "Reportar nueva incidencia"
                    }
                    .clickable { mostrarDialogo = true },
                shape = RoundedCornerShape(28.dp),
                color = surfaceColor,
                border = BorderStroke(1.dp, onSurface.copy(alpha = 0.1f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Box(modifier = Modifier.size(32.dp).background(primaryColor, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Reportar Incidencia",
                        color = onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoReporte(
            rutasSugeridas = viewModel.servicioHorarios.lineasPublicas.map { it.codigo },
            onDismiss = { mostrarDialogo = false },
            onConfirm = { nuevaRuta, nuevaDescripcion ->
                viewModel.agregarIncidencia(
                    Incidencia(
                        tipo = "Reporte Vecinal",
                        ruta = nuevaRuta.take(2).uppercase(),
                        titulo = "Incidencia en Ruta $nuevaRuta",
                        descripcion = nuevaDescripcion,
                        tiempo = "Hace un momento",
                        colorEtiqueta = Color(0xFF3498DB),
                        colorRuta = Color.Gray,
                        confirmaciones = 1,
                        esGlobal = false // Inicia local/cercana pendiente de verificación
                    )
                )
                mostrarDialogo = false
                mostrarExito = true
                reproducirSonidoNotificacion(context)
                Toast.makeText(context, "📍 Reporte enviado a usuarios cercanos para verificación", Toast.LENGTH_LONG).show()
            }
        )
    }
    if (mostrarExito) {
        AnimacionReporteExitoso(
            onFinished = {
                mostrarExito = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemAlertaDeslizable(
    incidencia: Incidencia,
    onEliminar: () -> Unit,
    onConfirmar: () -> Unit
) {
    var estaEliminado by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                estaEliminado = true
                true
            } else {
                false
            }
        }
    )

    LaunchedEffect(estaEliminado) {
        if (estaEliminado) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(250)
            onEliminar()
        }
    }

    AnimatedVisibility(
        visible = !estaEliminado,
        exit = shrinkVertically(animationSpec = tween(durationMillis = 250)) + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                val colorFondo = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFC0392B)
                    else -> Color.Transparent
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorFondo)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.semantics {
                            contentDescription = "Eliminar alerta de ruta ${incidencia.ruta}"
                        }
                    ) {
                        Text(
                            text = "Eliminar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            content = {
                TarjetaAlerta(
                    incidencia = incidencia,
                    onEliminarClick = {
                        estaEliminado = true
                    },
                    onConfirmarClick = onConfirmar
                )
            }
        )
    }
}

@Composable
fun TarjetaAlerta(
    incidencia: Incidencia,
    onEliminarClick: () -> Unit = {},
    onConfirmarClick: () -> Unit = {}
) {
    var expandida by remember { mutableStateOf(true) }
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val (iconoIncidencia, colorIconoFondo) = when {
        incidencia.tipo.contains("Retraso", ignoreCase = true) -> Pair(Icons.Default.Warning, Color(0xFFFADBD8))
        incidencia.tipo.contains("Desvío", ignoreCase = true) -> Pair(Icons.AutoMirrored.Filled.Shortcut, Color(0xFFFDEBD0))
        else -> Pair(Icons.Default.Campaign, Color(0xFFEBF5FB))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "Alerta ${incidencia.tipo} en ruta ${incidencia.ruta}: ${incidencia.titulo}. ${incidencia.descripcion}. Registrado ${incidencia.tiempo}"
            }
            .clickable { expandida = !expandida },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val (routeSurface, typeSurface, timeText, deleteButton, expandIcon, iconBox, titleText, descText) = createRefs()

            Surface(
                color = incidencia.colorRuta,
                shape = CircleShape,
                modifier = Modifier
                    .size(34.dp)
                    .constrainAs(routeSurface) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = incidencia.ruta,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Surface(
                color = incidencia.colorEtiqueta,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.constrainAs(typeSurface) {
                    top.linkTo(routeSurface.top)
                    bottom.linkTo(routeSurface.bottom)
                    start.linkTo(routeSurface.end, margin = 8.dp)
                    end.linkTo(timeText.start, margin = 6.dp)
                    width = Dimension.preferredWrapContent
                }
            ) {
                Text(
                    text = incidencia.tipo,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Text(
                text = incidencia.tiempo,
                fontSize = 11.sp,
                color = onSurfaceColor.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.constrainAs(timeText) {
                    top.linkTo(routeSurface.top)
                    bottom.linkTo(routeSurface.bottom)
                    end.linkTo(deleteButton.start, margin = 8.dp)
                }
            )

            IconButton(
                onClick = onEliminarClick,
                modifier = Modifier
                    .size(28.dp)
                    .constrainAs(deleteButton) {
                        top.linkTo(routeSurface.top)
                        bottom.linkTo(routeSurface.bottom)
                        end.linkTo(expandIcon.start, margin = 8.dp)
                    }
                    .semantics {
                        role = Role.Button
                        contentDescription = "Eliminar esta alerta"
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar alerta",
                    tint = Color(0xFFC0392B).copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Icon(
                imageVector = if (expandida) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expandida) "Contraer" else "Expandir",
                tint = onSurfaceColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(20.dp)
                    .constrainAs(expandIcon) {
                        top.linkTo(routeSurface.top)
                        bottom.linkTo(routeSurface.bottom, margin = 2.dp)
                        end.linkTo(parent.end)
                    }
            )

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(colorIconoFondo, RoundedCornerShape(12.dp))
                    .constrainAs(iconBox) {
                        top.linkTo(routeSurface.bottom, margin = 12.dp)
                        start.linkTo(parent.start)
                        if (!expandida) {
                            bottom.linkTo(parent.bottom)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconoIncidencia,
                    contentDescription = null,
                    tint = incidencia.colorEtiqueta,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = incidencia.titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = onSurfaceColor,
                modifier = Modifier.constrainAs(titleText) {
                    top.linkTo(iconBox.top)
                    start.linkTo(iconBox.end, margin = 12.dp)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    if (!expandida) {
                        bottom.linkTo(parent.bottom)
                    }
                }
            )

            if (expandida) {
                Column(
                    modifier = Modifier.constrainAs(descText) {
                        top.linkTo(titleText.bottom, margin = 6.dp)
                        start.linkTo(iconBox.end, margin = 12.dp)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                        bottom.linkTo(parent.bottom)
                    }
                ) {
                    Text(
                        text = incidencia.descripcion,
                        fontSize = 13.sp,
                        color = onSurfaceColor.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (incidencia.esGlobal) "🟢 Verificada (${incidencia.confirmaciones} confirmaciones)" else "🟡 Pendiente de confirmación cercana (${incidencia.confirmaciones}/2)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (incidencia.esGlobal) Color(0xFF2ECC71) else Color(0xFFF39C12)
                        )

                        if (!incidencia.esGlobal) {
                            OutlinedButton(
                                onClick = onConfirmarClick,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("✓ Confirmar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoReporte(
    rutasSugeridas: List<String> = listOf("L1", "L4", "L5", "L7", "MA"),
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var ruta by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    var errorRuta by remember { mutableStateOf(false) }
    var errorDescripcion by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar Incidencia") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = ruta,
                    onValueChange = { ruta = it },
                    label = { Text("Línea o Ruta (Ej. L1, R10)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción del incidente") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ruta.isNotBlank() && descripcion.isNotBlank()) {
                        onConfirm(ruta, descripcion)
                    }
                }
            ) {
                Text("Enviar Reporte")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AnimacionReporteExitoso(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1200)
        onFinished()
    }
    AlertDialog(
        onDismissRequest = {},
        title = { Text("¡Reporte Enviado!") },
        text = { Text("Tu reporte ha sido enviado a la comunidad para su verificación cercana.") },
        confirmButton = {}
    )
}
