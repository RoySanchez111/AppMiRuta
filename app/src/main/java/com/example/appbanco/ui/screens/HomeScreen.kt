package com.example.appbanco.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.appbanco.data.database.ConductorUbicacion
import com.example.appbanco.data.database.SyncManager
import com.example.appbanco.logic.SessionManager
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class ParadaMapa(
    val id: String,
    val nombre: String,
    val lineas: List<String>,
    val ubicacion: Point,
    val proximaLlegada: String,
    val esIncidencia: Boolean = false,
    val detalleIncidencia: String? = null
)

val paradasMapbox = listOf(
    ParadaMapa("1", "Tecmilenio Campus Puebla", listOf("L1", "L4"), Point.fromLngLat(-98.261833, 18.999446), "En 2 min"),
    ParadaMapa("2", "Plaza Mayor", listOf("L1", "L7"), Point.fromLngLat(-98.255000, 19.005000), "En 5 min"),
    ParadaMapa("3", "Universidad CCU", listOf("L4", "MA"), Point.fromLngLat(-98.268000, 18.992000), "En 3 min"),
    ParadaMapa("4", "Hospital Norte", listOf("L7", "L5"), Point.fromLngLat(-98.248000, 19.012000), "En 9 min"),
    ParadaMapa("5", "Aeropuerto / Terminal", listOf("L1"), Point.fromLngLat(-98.240000, 19.020000), "En 12 min"),
    ParadaMapa("6", "Alerta L5 - Calzada Serdán", listOf("L5"), Point.fromLngLat(-98.251000, 19.008000), "Retraso 15 min", esIncidencia = true, detalleIncidencia = "Falla técnica en vía - Use rutas alternas")
)

@Composable
fun PantallaPrincipal(navController: NavController) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val sessionManager = remember { SessionManager(context) }
    val currentUsernameState = sessionManager.currentUsername.collectAsState(initial = "Pasajero")
    val userRoleState = sessionManager.userRole.collectAsState(initial = "pasajero")
    var rolUsuario by remember(userRoleState.value) { mutableStateOf(userRoleState.value) }
    val nombreUsuario = currentUsernameState.value ?: "Pasajero"

    var transmitiendoUbicacion by remember { mutableStateOf(false) }
    var conductoresActivos by remember { mutableStateOf<List<ConductorUbicacion>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val syncManager = remember { SyncManager() }

    val tecmilenioPuebla = remember { Point.fromLngLat(-98.261833, 18.999446) }

    var busquedaTexto by remember { mutableStateOf("") }
    var filtroActivo by remember { mutableStateOf("Todas") }
    var paradaSeleccionada by remember { mutableStateOf<ParadaMapa?>(null) }
    var ubicacionGpsPoint by remember { mutableStateOf<Point?>(null) }
    var textoCoordenadas by remember { mutableStateOf("18.9994° N, 98.2618° W") }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun obtenerUbicacionGpsReal(onSuccess: (Point) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        val pt = Point.fromLngLat(loc.longitude, loc.latitude)
                        textoCoordenadas = String.format(Locale.US, "%.4f° N, %.4f° W", loc.latitude, Math.abs(loc.longitude))
                        onSuccess(pt)
                    } else {
                        onSuccess(tecmilenioPuebla)
                    }
                }.addOnFailureListener {
                    onSuccess(tecmilenioPuebla)
                }
            } catch (e: Exception) {
                onSuccess(tecmilenioPuebla)
            }
        } else {
            onSuccess(tecmilenioPuebla)
        }
    }

    // Cargar y actualizar conductores activos de la nube periódicamente (Free Tier Optimizado)
    LaunchedEffect(Unit) {
        while (isActive) {
            syncManager.fetchConductoresActivos { lista ->
                conductoresActivos = lista
            }
            delay(6000)
        }
    }

    // Transmisión GPS en vivo cuando el rol es Conductor
    LaunchedEffect(transmitiendoUbicacion) {
        if (transmitiendoUbicacion) {
            while (isActive && transmitiendoUbicacion) {
                obtenerUbicacionGpsReal { realPoint ->
                    scope.launch {
                        syncManager.broadcastConductorLocation(
                            conductorId = nombreUsuario,
                            nombre = "Conductor $nombreUsuario",
                            ruta = "Línea L1",
                            lat = realPoint.latitude(),
                            lng = realPoint.longitude(),
                            activo = true
                        )
                    }
                }
                delay(5000)
            }
        }
    }

    val paradasFiltradas by remember(busquedaTexto, filtroActivo) {
        derivedStateOf {
            paradasMapbox.filter { parada ->
                val coincideTexto = busquedaTexto.isBlank() || 
                    parada.nombre.contains(busquedaTexto, ignoreCase = true) ||
                    parada.lineas.any { it.contains(busquedaTexto, ignoreCase = true) }
                val coincideFiltro = when (filtroActivo) {
                    "Incidencias" -> parada.esIncidencia
                    "Directos" -> parada.lineas.contains("L1") || parada.lineas.contains("L4")
                    else -> true
                }
                coincideTexto && coincideFiltro
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // BUSCADOR PRINCIPAL
        OutlinedTextField(
            value = busquedaTexto,
            onValueChange = { busquedaTexto = it },
            placeholder = { Text("Buscar ruta, parada o destino en Puebla...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = if (busquedaTexto.isNotEmpty()) {
                {
                    IconButton(onClick = { busquedaTexto = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                    }
                }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = surfaceColor,
                focusedContainerColor = surfaceColor,
                unfocusedBorderColor = onBackground.copy(alpha = 0.15f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // CHIPS DE FILTRO RÁPIDO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todas", "Directos", "Incidencias").forEach { filtro ->
                val seleccionado = filtroActivo == filtro
                FilterChip(
                    selected = seleccionado,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        filtroActivo = filtro
                    },
                    label = { Text(filtro, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    leadingIcon = if (seleccionado) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // RESULTADOS DE BÚSQUEDA RÁPIDA
        if (busquedaTexto.isNotBlank()) {
            Text(
                text = "Resultados para '${busquedaTexto}':",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = onBackground.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (paradasFiltradas.isEmpty()) {
                Text(
                    text = "No se encontraron paradas que coincidan.",
                    fontSize = 12.sp,
                    color = onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    paradasFiltradas.take(3).forEach { parada ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    paradaSeleccionada = parada
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (parada.esIncidencia) Icons.Default.Warning else Icons.Default.Place,
                                    contentDescription = null,
                                    tint = if (parada.esIncidencia) Color(0xFFC0392B) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(parada.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onSurface)
                                    Text("Líneas: ${parada.lineas.joinToString(", ")} • ${parada.proximaLlegada}", fontSize = 12.sp, color = onSurface.copy(alpha = 0.6f))
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = onSurface.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MAPA INTERACTIVO NATIVO EN MAPBOX COMPOSE V11
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .clip(RoundedCornerShape(20.dp))
                .semantics {
                    contentDescription = "Mapa interactivo nativo Mapbox con líneas de transporte y ubicaciones"
                }
        ) {
            MapaOptimizadoContainer(
                paradas = paradasFiltradas,
                conductores = conductoresActivos,
                tecmilenioPuebla = tecmilenioPuebla,
                ubicacionCentradaPoint = ubicacionGpsPoint,
                onParadaSelect = { parada ->
                    paradaSeleccionada = parada
                },
                modifier = Modifier.fillMaxSize()
            )

            // INSIGNIA FLOTANTE DE PERFIL Y CAMBIO DE ROL (TopStart)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .shadow(6.dp, RoundedCornerShape(20.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val nuevoRol = if (rolUsuario == "conductor") "pasajero" else "conductor"
                        rolUsuario = nuevoRol
                        scope.launch {
                            sessionManager.updateUserRole(nuevoRol)
                        }
                        Toast.makeText(context, "👤 Rol cambiado a: ${nuevoRol.uppercase()}", Toast.LENGTH_SHORT).show()
                    },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            shape = CircleShape,
                            color = if (rolUsuario == "conductor") Color(0xFFE67E22) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (rolUsuario == "conductor") Icons.Default.DirectionsBus else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        // Indicador Verde "En línea / GPS Activo"
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFF2ECC71), CircleShape)
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = nombreUsuario,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = onSurface
                        )
                        Text(
                            text = if (rolUsuario == "conductor") "🚌 Conductor L1" else "Pasajero • GPS",
                            fontSize = 10.sp,
                            color = if (rolUsuario == "conductor") Color(0xFFE67E22) else Color(0xFF2ECC71),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // PANEL INFERIOR IZQUIERDO DE COORDENADAS GPS (BottomStart)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, onBackground.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = Color(0xFF4285F4),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = textoCoordenadas,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )
                }
            }

            // CONTROLES SUPERIORES DERECHOS
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // BOTÓN RECARGAR DATOS DE CONDUCTORES
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        scope.launch {
                            syncManager.fetchConductoresActivos { lista ->
                                conductoresActivos = lista
                            }
                        }
                        Toast.makeText(context, "🚌 Posiciones de conductores actualizadas", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .background(Color.White, CircleShape)
                        .shadow(4.dp, CircleShape)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Actualizar posiciones de autobuses en tiempo real"
                        }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                // SI ES CONDUCTOR: BOTÓN TRANSMITIR UBICACIÓN GPS EN VIVO
                if (rolUsuario == "conductor") {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            transmitiendoUbicacion = !transmitiendoUbicacion
                            Toast.makeText(
                                context,
                                if (transmitiendoUbicacion) "📡 Transmitiendo ubicación GPS en tiempo real..." else "⏸️ Transmisión en vivo pausada",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (transmitiendoUbicacion) Color(0xFFE74C3C) else Color(0xFF2ECC71),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.shadow(4.dp, RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = if (transmitiendoUbicacion) Icons.Default.Sensors else Icons.Default.SensorsOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (transmitiendoUbicacion) "EN VIVO 📡" else "Transmitir",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // BOTÓN CENTRAR EN MI UBICACIÓN GPS REAL (BottomEnd)
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    obtenerUbicacionGpsReal { realPoint ->
                        ubicacionGpsPoint = realPoint
                        Toast.makeText(context, "📍 Centrado en tus coordenadas GPS reales", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Color.White, CircleShape)
                    .shadow(4.dp, CircleShape)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Centrar mapa en mi ubicación GPS"
                    }
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF4285F4))
            }
        }

        if (paradaSeleccionada != null) {
            val parada = paradaSeleccionada!!
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Detalles de parada ${parada.nombre}, próximas salidas ${parada.proximaLlegada}"
                    },
                shape = RoundedCornerShape(20.dp),
                color = surfaceColor,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, onBackground.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (parada.esIncidencia) Icons.Default.Warning else Icons.Default.Place,
                                contentDescription = null,
                                tint = if (parada.esIncidencia) Color(0xFFC0392B) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(parada.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurface)
                        }
                        IconButton(onClick = { paradaSeleccionada = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar detalles de parada")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (parada.esIncidencia && parada.detalleIncidencia != null) {
                        Surface(
                            color = Color(0xFFFDEDEC),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFC0392B), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(parada.detalleIncidencia, color = Color(0xFFC0392B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text("Líneas que transitan:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onSurface.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        parada.lineas.forEach { linea ->
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(linea, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Próximo autobús: ${parada.proximaLlegada}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Button(
                            onClick = {
                                Toast.makeText(context, "Calculando mejor ruta hacia ${parada.nombre}...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Cómo llegar a ${parada.nombre}"
                                },
                            colors = ButtonDefaults.buttonColors(containerColor = onSurface.copy(alpha = 0.1f), contentColor = onSurface)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cómo llegar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = onSurface.copy(alpha = 0.1f)
                    )

                    // HERRAMIENTAS CONECTADAS DE LA APP
                    Text(
                        text = "Herramientas Conectadas:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // BOTÓN VER HORARIOS
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                navController.navigate("horarios")
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Horarios", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // BOTÓN ALERTAS / INCIDENCIAS
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                navController.navigate("alertas")
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (parada.esIncidencia) Color(0xFFC0392B) else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Alertas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // BOTÓN RUTA FRECUENTE
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                navController.navigate("frecuentes")
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guardar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun MapaOptimizadoContainer(
    paradas: List<ParadaMapa>,
    conductores: List<ConductorUbicacion> = emptyList(),
    tecmilenioPuebla: Point,
    ubicacionCentradaPoint: Point? = null,
    onParadaSelect: (ParadaMapa) -> Unit,
    modifier: Modifier = Modifier
) {
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(tecmilenioPuebla)
            zoom(13.8)
            pitch(30.0)
        }
    }

    LaunchedEffect(ubicacionCentradaPoint) {
        if (ubicacionCentradaPoint != null) {
            mapViewportState.flyTo(
                CameraOptions.Builder()
                    .center(ubicacionCentradaPoint)
                    .zoom(16.0)
                    .build()
            )
        }
    }

    val rutaNaranjaPoints = remember {
        listOf(
            Point.fromLngLat(-98.261833, 18.999446),
            Point.fromLngLat(-98.255000, 19.005000),
            Point.fromLngLat(-98.240000, 19.020000)
        )
    }

    val rutaAzulPoints = remember {
        listOf(
            Point.fromLngLat(-98.268000, 18.992000),
            Point.fromLngLat(-98.261833, 18.999446),
            Point.fromLngLat(-98.248000, 19.012000)
        )
    }

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { MapStyle("https://tiles.openfreemap.org/styles/bright") }
        ) {
            MapEffect(Unit) { mapView ->
                try {
                    mapView.location.updateSettings {
                        enabled = true
                        pulsingEnabled = true
                        locationPuck = createDefault2DPuck(withBearing = true)
                    }
                    mapView.setOnTouchListener { v, _ ->
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                } catch (e: Exception) { }
            }

            PolylineAnnotation(
                points = rutaNaranjaPoints,
                lineColorString = "#FF8E56",
                lineWidth = 5.0
            )

            PolylineAnnotation(
                points = rutaAzulPoints,
                lineColorString = "#327CF2",
                lineWidth = 5.0
            )

            paradas.forEach { parada ->
                PointAnnotation(
                    point = parada.ubicacion,
                    onClick = {
                        onParadaSelect(parada)
                        mapViewportState.flyTo(
                            CameraOptions.Builder()
                                .center(parada.ubicacion)
                                .zoom(15.5)
                                .build()
                        )
                        true
                    }
                )
            }

            conductores.forEach { conductor ->
                PointAnnotation(
                    point = Point.fromLngLat(conductor.lng, conductor.lat),
                    onClick = {
                        mapViewportState.flyTo(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(conductor.lng, conductor.lat))
                                .zoom(16.0)
                                .build()
                        )
                        true
                    }
                )
            }
        }
    }
}
