package com.example.appbanco.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.appbanco.data.database.ConductorUbicacion
import com.example.appbanco.data.database.SyncManager
import com.example.appbanco.logic.SessionManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
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

// Generador de paradas adaptativas en tiempo real según las coordenadas recibidas
fun generarParadasAdaptativas(centro: Point): List<ParadaMapa> {
    val lat = centro.latitude()
    val lng = centro.longitude()

    return listOf(
        ParadaMapa("1", "Parada Principal (GPS)", listOf("L1", "L4"), Point.fromLngLat(lng, lat), "En 2 min"),
        ParadaMapa("2", "Estación Norte", listOf("L1", "L7"), Point.fromLngLat(lng + 0.0068, lat + 0.0056), "En 5 min"),
        ParadaMapa("3", "Terminal Sur", listOf("L4", "MA"), Point.fromLngLat(lng - 0.0062, lat - 0.0074), "En 3 min"),
        ParadaMapa("4", "Hospital / Centro de Salud", listOf("L7", "L5"), Point.fromLngLat(lng + 0.0138, lat + 0.0126), "En 9 min"),
        ParadaMapa("5", "Terminal Express", listOf("L1"), Point.fromLngLat(lng + 0.0218, lat + 0.0206), "En 12 min"),
        ParadaMapa("6", "Alerta L5 - Av. Principal", listOf("L5"), Point.fromLngLat(lng - 0.0038, lat + 0.0086), "Retraso 15 min", esIncidencia = true, detalleIncidencia = "Tráfico denso en vía principal - Use rutas alternas")
    )
}

@Composable
fun PantallaPrincipal(navController: NavController) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val sessionManager = remember { SessionManager(context) }
    val currentUsernameState = sessionManager.currentUsername.collectAsState(initial = "Usuario")
    val nombreUsuario = currentUsernameState.value ?: "Usuario"

    var conductoresActivos by remember { mutableStateOf<List<ConductorUbicacion>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val syncManager = remember { SyncManager() }

    val centroPredeterminado = remember { Point.fromLngLat(-98.261833, 18.999446) }

    var busquedaTexto by remember { mutableStateOf("") }
    var filtroActivo by remember { mutableStateOf("Todas") }
    var paradaSeleccionada by remember { mutableStateOf<ParadaMapa?>(null) }
    var ubicacionGpsPoint by remember { mutableStateOf<Point?>(null) }
    var textoCoordenadas by remember { mutableStateOf("18.9994° N, 98.2618° W") }

    val centroActual = ubicacionGpsPoint ?: centroPredeterminado
    val paradasAdaptativas = remember(centroActual) { generarParadasAdaptativas(centroActual) }

    val fusedLocationClient = remember(context) { LocationServices.getFusedLocationProviderClient(context.applicationContext) }

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
                        // Fallback de alta precisión para dispositivos físicos cuando lastLocation es null
                        try {
                            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                .addOnSuccessListener { currentLoc ->
                                    if (currentLoc != null) {
                                        val pt = Point.fromLngLat(currentLoc.longitude, currentLoc.latitude)
                                        textoCoordenadas = String.format(Locale.US, "%.4f° N, %.4f° W", currentLoc.latitude, Math.abs(currentLoc.longitude))
                                        onSuccess(pt)
                                    } else {
                                        onSuccess(centroPredeterminado)
                                    }
                                }
                                .addOnFailureListener { onSuccess(centroPredeterminado) }
                        } catch (e: Exception) {
                            onSuccess(centroPredeterminado)
                        }
                    }
                }.addOnFailureListener {
                    onSuccess(centroPredeterminado)
                }
            } catch (e: Exception) {
                onSuccess(centroPredeterminado)
            }
        } else {
            onSuccess(centroPredeterminado)
        }
    }

    var tienePermisoUbicacion by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fine || coarse) {
            tienePermisoUbicacion = true
            Toast.makeText(context, "📍 Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
            obtenerUbicacionGpsReal { pt ->
                ubicacionGpsPoint = pt
            }
        } else {
            tienePermisoUbicacion = false
            Toast.makeText(context, "⚠️ Permiso de ubicación denegado. Usando ubicación predeterminada", Toast.LENGTH_LONG).show()
        }
    }

    // Solicitar permisos automáticamente si no están concedidos al cargar la pantalla
    LaunchedEffect(Unit) {
        if (!tienePermisoUbicacion) {
            val perms = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(perms.toTypedArray())
        } else {
            obtenerUbicacionGpsReal { pt ->
                ubicacionGpsPoint = pt
            }
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



    val paradasFiltradas by remember(busquedaTexto, filtroActivo, paradasAdaptativas) {
        derivedStateOf {
            paradasAdaptativas.filter { parada ->
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

    val configuration = LocalConfiguration.current
    val esPantallaAncha = configuration.screenWidthDp >= 600

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = if (esPantallaAncha) 900.dp else Dp.Unspecified)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // TARJETA INTERACTIVA DE SOLICITUD DE PERMISOS DE UBICACIÓN Y NOTIFICACIONES
        if (!tienePermisoUbicacion) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val perms = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            perms.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(perms.toTypedArray())
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Solicitar Permisos de Ubicación",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = onSurface
                        )
                        Text(
                            text = "Habilita el GPS para ver autobuses en vivo y centrar tu posición",
                            fontSize = 11.sp,
                            color = onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Button(
                        onClick = {
                            val perms = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                perms.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissionLauncher.launch(perms.toTypedArray())
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Activar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

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



        Spacer(modifier = Modifier.height(10.dp))

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
                text = "Resultados para '$busquedaTexto':",
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
                                    busquedaTexto = ""
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



        Spacer(modifier = Modifier.height(10.dp))

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
                centroPoint = centroActual,
                ubicacionCentradaPoint = ubicacionGpsPoint,
                onParadaSelect = { parada ->
                    paradaSeleccionada = parada
                },
                modifier = Modifier.fillMaxSize()
            )

            // INSIGNIA FLOTANTE DE PERFIL EN EL MAPA (TopStart)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
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
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        // Indicador Verde "GPS Activo"
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
                            text = "GPS Activo",
                            fontSize = 10.sp,
                            color = Color(0xFF2ECC71),
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

            // CONTROLES DE ACCIÓN DEL MAPA (M3 UNIFICADO)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                // 1. BOTÓN RECALCULAR / ACTUALIZAR RUTAS Y CONDUCTORES
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        scope.launch {
                            syncManager.fetchConductoresActivos { lista ->
                                conductoresActivos = lista
                            }
                        }
                        Toast.makeText(context, "🔄 Rutas y autobuses recalculados en tiempo real", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = surfaceColor.copy(alpha = 0.95f),
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "Recalcular y actualizar posiciones de autobuses"
                    }
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 2. BOTÓN CENTRAR EN MI UBICACIÓN GPS REAL
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (!tienePermisoUbicacion) {
                            val perms = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                perms.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissionLauncher.launch(perms.toTypedArray())
                        } else {
                            obtenerUbicacionGpsReal { realPoint ->
                                ubicacionGpsPoint = realPoint
                                Toast.makeText(context, "🎯 Mapa centrado en tu posición GPS", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp,
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "Centrar mapa en mi ubicación GPS"
                    }
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
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
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ReportProblem, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(parada.detalleIncidencia, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
}

@Composable
fun MapaOptimizadoContainer(
    paradas: List<ParadaMapa>,
    conductores: List<ConductorUbicacion> = emptyList(),
    centroPoint: Point,
    ubicacionCentradaPoint: Point? = null,
    onParadaSelect: (ParadaMapa) -> Unit,
    modifier: Modifier = Modifier
) {
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(centroPoint)
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

    // Polilíneas dinámicas generadas a partir de las paradas adaptativas
    val rutaNaranjaPoints = remember(paradas) {
        if (paradas.size >= 5) {
            listOf(paradas[0].ubicacion, paradas[1].ubicacion, paradas[4].ubicacion)
        } else paradas.map { it.ubicacion }
    }

    val rutaAzulPoints = remember(paradas) {
        if (paradas.size >= 4) {
            listOf(paradas[2].ubicacion, paradas[0].ubicacion, paradas[3].ubicacion)
        } else paradas.map { it.ubicacion }
    }

    val conductoresAdaptativos = remember(conductores, centroPoint) {
        if (conductores.isNotEmpty()) conductores else listOf(
            ConductorUbicacion(
                id = "bus_l1_live",
                nombre = "Autobús L1 (En Vivo)",
                ruta = "Línea L1",
                lat = centroPoint.latitude() + 0.0028,
                lng = centroPoint.longitude() - 0.0035,
                activo = true
            ),
            ConductorUbicacion(
                id = "bus_l4_live",
                nombre = "Autobús L4 (En Vivo)",
                ruta = "Línea L4",
                lat = centroPoint.latitude() - 0.0042,
                lng = centroPoint.longitude() + 0.0051,
                activo = true
            )
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

            conductoresAdaptativos.forEach { conductor ->
                CircleAnnotation(
                    point = Point.fromLngLat(conductor.lng, conductor.lat),
                    circleRadius = 14.0,
                    circleColorString = "#E67E22",
                    circleStrokeWidth = 3.0,
                    circleStrokeColorString = "#FFFFFF",
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
