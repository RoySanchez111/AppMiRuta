package com.example.appbanco.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.appbanco.ui.viewmodel.RutaFrecuenteItem
import com.google.android.gms.location.LocationServices
import java.util.Locale

@Composable
fun DialogoAgregarRutaFrecuente(
    onDismiss: () -> Unit,
    onConfirmar: (RutaFrecuenteItem) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var nombre by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var colorSeleccionado by remember { mutableStateOf(Color(0xFF4A86F7)) }
    var iconoSeleccionado by remember { mutableStateOf(Icons.Default.Home) }
    var errorMsg by remember { mutableStateOf("") }

    val fusedLocationClientDialog = remember { LocationServices.getFusedLocationProviderClient(context) }

    val sugerenciasUbicacion = listOf(
        "Tecmilenio Campus Puebla",
        "Plaza Mayor Puebla",
        "Universidad CCU Angelópolis",
        "Hospital Norte Puebla",
        "Centro Histórico",
        "Aeropuerto Hermanos Serdán"
    )

    val sugerenciasFiltradas = remember(ubicacion) {
        if (ubicacion.isBlank()) emptyList()
        else sugerenciasUbicacion.filter { it.contains(ubicacion, ignoreCase = true) && it != ubicacion }
    }

    val coloresDisponibles = listOf(
        Color(0xFF4A86F7),
        Color(0xFFF26E68),
        Color(0xFF2ECC71),
        Color(0xFF9B59B6),
        Color(0xFFF39C12),
        Color(0xFF1ABC9C)
    )

    val iconosDisponibles = listOf(
        Pair(Icons.Default.Home, "Casa"),
        Pair(Icons.Default.School, "Escuela"),
        Pair(Icons.Default.Work, "Trabajo"),
        Pair(Icons.Default.FitnessCenter, "Gym"),
        Pair(Icons.Default.ShoppingCart, "Compras"),
        Pair(Icons.Default.Place, "Lugar")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Agregar Ruta Frecuente",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        errorMsg = ""
                    },
                    label = { Text("Nombre / Etiqueta (Ej. Trabajo)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            try {
                                fusedLocationClientDialog.lastLocation.addOnSuccessListener { loc ->
                                    if (loc != null) {
                                        ubicacion = String.format(Locale.US, "GPS: %.4f, %.4f", loc.latitude, loc.longitude)
                                    } else {
                                        ubicacion = "Tecmilenio Campus Puebla (GPS)"
                                    }
                                    errorMsg = ""
                                }.addOnFailureListener {
                                    ubicacion = "Tecmilenio Campus Puebla (GPS)"
                                    errorMsg = ""
                                }
                            } catch (e: Exception) {
                                ubicacion = "Tecmilenio Campus Puebla (GPS)"
                                errorMsg = ""
                            }
                        } else {
                            ubicacion = "Tecmilenio Campus Puebla (GPS)"
                            errorMsg = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📍 Obtener mi posición GPS real", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = {
                        ubicacion = it
                        errorMsg = ""
                    },
                    label = { Text("Ubicación o Dirección") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = if (ubicacion.isNotEmpty()) {
                        {
                            IconButton(onClick = { ubicacion = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar ubicación")
                            }
                        }
                    } else null
                )

                if (sugerenciasFiltradas.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Column {
                            sugerenciasFiltradas.take(3).forEach { sugerencia ->
                                Text(
                                    text = "📍 $sugerencia",
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            ubicacion = sugerencia
                                            errorMsg = ""
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Ubicaciones rápidas:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sugerenciasUbicacion.take(4).forEach { lugarChip ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                            modifier = Modifier.clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                ubicacion = lugarChip
                                errorMsg = ""
                            }
                        ) {
                            Text(lugarChip, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Seleccionar Icono:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    iconosDisponibles.forEach { (iconoPair, desc) ->
                        val esSeleccionado = iconoSeleccionado == iconoPair
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (esSeleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Icono $desc"
                                }
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    iconoSeleccionado = iconoPair
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconoPair,
                                contentDescription = null,
                                tint = if (esSeleccionado) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Seleccionar Color:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    coloresDisponibles.forEach { color ->
                        val esSeleccionado = colorSeleccionado == color
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (esSeleccionado) 3.dp else 0.dp,
                                    color = if (esSeleccionado) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    colorSeleccionado = color
                                }
                        )
                    }
                }

                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMsg, 
                        color = MaterialTheme.colorScheme.error, 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nombreLimpio = nombre.trim()
                    val ubicacionLimpia = ubicacion.trim()

                    if (nombreLimpio.isBlank() && ubicacionLimpia.isBlank()) {
                        errorMsg = "Debes ingresar el nombre y la ubicación de la ruta"
                    } else if (nombreLimpio.isBlank()) {
                        errorMsg = "Escribe un nombre o etiqueta para la ruta"
                    } else if (ubicacionLimpia.isBlank()) {
                        errorMsg = "Ingresa o selecciona una ubicación válida"
                    } else {
                        val idNueva = System.currentTimeMillis().toString()
                        onConfirmar(
                            RutaFrecuenteItem(
                                id = idNueva,
                                nombre = nombreLimpio,
                                ubicacion = ubicacionLimpia,
                                color = colorSeleccionado,
                                icono = iconoSeleccionado
                            )
                        )
                    }
                }
            ) {
                Text("Guardar Ruta")
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
fun DialogoOpcionesRutaFrecuente(
    ruta: RutaFrecuenteItem,
    onDismiss: () -> Unit,
    onVerEnMapa: () -> Unit,
    onEliminar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(ruta.icono, contentDescription = null, tint = ruta.color, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = ruta.nombre, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(text = "Ubicación: ${ruta.ubicacion}", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onVerEnMapa()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Place, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver en el Mapa")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onEliminar()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar de Frecuentes")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
