package com.example.appbanco.ui.screens.conductor.configuracion
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appbanco.ui.components.obtenerColoresFondo

@Composable
fun PantallaConfiguracionConductor(
    navController: NavController
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // -------------------------
    // ESTADOS
    // -------------------------
    var notifTiempoReal by remember { mutableStateOf(true) }
    var notifRetrasos by remember { mutableStateOf(true) }
    var sonidoVibracion by remember { mutableStateOf(false) }

    var modoOffline by remember { mutableStateOf(true) }
    var unidadDistancia by remember { mutableStateOf("km") }
    var temaSeleccionado by remember { mutableStateOf("Auto") }

    // -------------------------
    // COLORES
    // -------------------------
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    // -------------------------
    // TEMA SEGÚN LA HORA
    // -------------------------
    val coloresFondo = remember {
        obtenerColoresFondo()
    }

    val fondoHorario = remember(coloresFondo) {
        Brush.horizontalGradient(coloresFondo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {

        // =========================================
        // ENCABEZADO
        // =========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                    .statusBarsPadding()
                    .heightIn(min = 115.dp)
                    .padding(
                        horizontal = 14.dp,
                        vertical = 18.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White,
                        modifier = Modifier.size(27.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Column {
                    Text(
                        text = "Cuenta",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )

                    Text(
                        text = "Configuración",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Ajustes del conductor",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // =========================================
        // CONTENIDO
        // =========================================
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 16.dp,
                    vertical = 18.dp
                )
        ) {

            // =====================================
            // NOTIFICACIONES
            // =====================================
            TituloSeccionConductor(
                texto = "Notificaciones"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.5f
                        )
                ),
                border = BorderStroke(
                    1.dp,
                    primaryColor.copy(alpha = 0.12f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    OpcionSwitchConductor(
                        titulo = "Notificaciones en tiempo real",
                        descripcion = "Alertas en tiempo real",
                        checked = notifTiempoReal,
                        onCheckedChange = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            )

                            notifTiempoReal = it
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = onSurfaceColor.copy(alpha = 0.08f)
                    )

                    OpcionSwitchConductor(
                        titulo = "Retraso de mis líneas",
                        descripcion = "Alertas de incidentes en tus líneas",
                        checked = notifRetrasos,
                        onCheckedChange = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            )

                            notifRetrasos = it
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = onSurfaceColor.copy(alpha = 0.08f)
                    )

                    OpcionSwitchConductor(
                        titulo = "Sonido y vibración",
                        descripcion = "Solo alertas graves",
                        checked = sonidoVibracion,
                        onCheckedChange = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            )

                            sonidoVibracion = it
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // =====================================
            // VIAJES
            // =====================================
            TituloSeccionConductor(
                texto = "Viajes"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.5f
                        )
                ),
                border = BorderStroke(
                    1.dp,
                    primaryColor.copy(alpha = 0.12f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    OpcionSwitchConductor(
                        titulo = "Modo Offline",
                        descripcion = "Sin datos",
                        checked = modoOffline,
                        onCheckedChange = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            )

                            modoOffline = it
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = onSurfaceColor.copy(alpha = 0.08f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Rutas descargadas",
                                color = onSurfaceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Text(
                                text = "24 MB",
                                color = onSurfaceColor.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }

                        TextButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Rutas actualizadas",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Text(
                                text = "Actualizar",
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = onSurfaceColor.copy(alpha = 0.08f)
                    )

                    Text(
                        text = "Unidad de distancia",
                        color = onSurfaceColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    SelectorConductor(
                        opciones = listOf(
                            "km",
                            "m",
                            "millas"
                        ),
                        seleccion = unidadDistancia,
                        onSeleccion = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            )

                            unidadDistancia = it
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // =====================================
            // APARIENCIA
            // =====================================
            TituloSeccionConductor(
                texto = "Apariencia"
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.5f
                        )
                ),
                border = BorderStroke(
                    1.dp,
                    primaryColor.copy(alpha = 0.12f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {

                    Text(
                        text = "Tema de la aplicación",
                        color = onSurfaceColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    SelectorConductor(
                        opciones = listOf(
                            "Claro",
                            "Oscuro",
                            "Auto"
                        ),
                        seleccion = temaSeleccionado,
                        onSeleccion = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            )

                            temaSeleccionado = it
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = if (temaSeleccionado == "Auto")
                            "El tema cambia automáticamente según la hora."
                        else
                            "Tema $temaSeleccionado seleccionado.",
                        color = onSurfaceColor.copy(alpha = 0.55f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )
        }
    }
}

// =====================================================
// TÍTULO DE SECCIÓN
// =====================================================
@Composable
private fun TituloSeccionConductor(
    texto: String
) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 17.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

// =====================================================
// OPCIÓN CON SWITCH
// =====================================================
@Composable
private fun OpcionSwitchConductor(
    titulo: String,
    descripcion: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val onSurfaceColor =
        MaterialTheme.colorScheme.onSurface

    val primaryColor =
        MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = titulo,
                color = onSurfaceColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                text = descripcion,
                color = onSurfaceColor.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primaryColor
            )
        )
    }
}

// =====================================================
// SELECTOR
// =====================================================
@Composable
private fun SelectorConductor(
    opciones: List<String>,
    seleccion: String,
    onSeleccion: (String) -> Unit
) {
    val onSurfaceColor =
        MaterialTheme.colorScheme.onSurface

    val primaryColor =
        MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = onSurfaceColor.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp)
        ) {

            opciones.forEach { opcion ->

                val seleccionada =
                    opcion == seleccion

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(
                            RoundedCornerShape(14.dp)
                        )
                        .background(
                            if (seleccionada)
                                primaryColor
                            else
                                Color.Transparent
                        )
                        .clickable {
                            onSeleccion(opcion)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opcion,
                        color =
                            if (seleccionada)
                                Color.White
                            else
                                onSurfaceColor.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        fontWeight =
                            if (seleccionada)
                                FontWeight.Bold
                            else
                                FontWeight.Medium
                    )
                }
            }
        }
    }
}