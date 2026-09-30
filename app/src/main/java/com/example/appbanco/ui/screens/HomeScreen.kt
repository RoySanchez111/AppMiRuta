package com.example.appbanco.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.example.appbanco.logic.ServicioOverpassPuebla
import com.example.appbanco.logic.ejecutarVibracionHaptica
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

// Generador de paradas adaptativas vacío (Sin Mock Data)
fun generarParadasAdaptativas(): List<ParadaMapa> {
    return emptyList()
}

@SuppressLint("ConfigurationScreenWidthHeight")
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
    val userRoleState = sessionManager.userRole.collectAsState(initial = "pasajero")
    val esConductor = userRoleState.value == "conductor" || userRoleState.value == "admin"

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
    val paradasAdaptativas = remember { generarParadasAdaptativas() }

    val fusedLocationClient = remember(context) { LocationServices.getFusedLocationProviderClient(context.applicationContext) }

    // 🌟 CONTROL DEL CICLO DE VIDA PARA DESMONTAJE LIGERO DEL MAPA
    val lifecycleOwner = LocalLifecycleOwner.current
    var isMapVisible by remember { mutableStateOf(true) }
    var mapReady by remember { mutableStateOf(false) } // Ocultar el "borrón" al INICIAR
    var isLeavingScreen by remember { mutableStateOf(false) } // Detectar SALIDA

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                isMapVisible = false
                isLeavingScreen = true
                mapReady = false
            } else if (event == Lifecycle.Event.ON_RESUME) {
                isMapVisible = true
                isLeavingScreen = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isMapVisible) {
        if (isMapVisible) {
            delay(50.milliseconds) // Micro-pausa mínima solo para permitir el renderizado del frame de Compose
            mapReady = true
        }
    }

    fun fallbackToNativeLocation(context: Context, onSuccess: (Point) -> Unit, defaultPoint: Point) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (hasFine || hasCoarse) {
                    var loc: Location? = null
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasFine) {
                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    }
                    if (loc == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    }
                    if (loc != null) {
                        onSuccess(Point.fromLngLat(loc.longitude, loc.latitude))
                        return
                    }
                }
            }
            onSuccess(defaultPoint)
        } catch (e: Exception) {
            onSuccess(defaultPoint)
        }
    }

    fun obtenerUbicacionGpsReal(onSuccess: (Point) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        val pt = Point.fromLngLat(loc.longitude, loc.latitude)
                        textoCoordenadas = String.format(Locale.US, "%.4f° N, %.4f° W", loc.latitude, abs(loc.longitude))
                        onSuccess(pt)
                    } else {
                        // Fallback de alta precisión para dispositivos físicos cuando lastLocation es null o GMS falla
                        try {
                            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                .addOnSuccessListener { currentLoc ->
                                    if (currentLoc != null) {
                                        val pt = Point.fromLngLat(currentLoc.longitude, currentLoc.latitude)
                                        textoCoordenadas = String.format(Locale.US, "%.4f° N, %.4f° W", currentLoc.latitude, abs(currentLoc.longitude))
                                        onSuccess(pt)
                                    } else {
                                        fallbackToNativeLocation(context, onSuccess, centroPredeterminado)
                                    }
                                }
                                .addOnFailureListener {
                                    fallbackToNativeLocation(context, onSuccess, centroPredeterminado)
                                }
                        } catch (e: Exception) {
                            fallbackToNativeLocation(context, onSuccess, centroPredeterminado)
                        }
                    }
                }.addOnFailureListener {
                    fallbackToNativeLocation(context, onSuccess, centroPredeterminado)
                }
            } catch (e: Exception) {
                fallbackToNativeLocation(context, onSuccess, centroPredeterminado)
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

    // MODO OFFLINE / AHORRO DE DATOS
    val modoOffline by sessionManager.modoOffline.collectAsState(initial = false)

    var rutasOsmReal by remember { mutableStateOf<List<List<Point>>>(emptyList()) }
    val overpassService = remember { ServicioOverpassPuebla() }

    LaunchedEffect(modoOffline) {
        if (!modoOffline) {
            val resultado = overpassService.obtenerCoordenadasRutasBusPuebla()
            if (resultado.isNotEmpty()) {
                rutasOsmReal = resultado
            }
        } else {
            rutasOsmReal = emptyList()
        }
    }

    // Cargar y actualizar conductores activos de la nube usando Sincronización Real de Firestore
    LaunchedEffect(modoOffline) {
        if (!modoOffline) {
            syncManager.observeConductoresActivos().collect { lista ->
                conductoresActivos = lista
                android.util.Log.d("LOCATION_SYNC", "Usuario recibió conductores: ${lista.map { it.id to it.activo }}")
            }
        } else {
            // Si entra en modo Offline, limpia los autobuses en vivo y ahorra batería/datos
            conductoresActivos = emptyList()
        }
    }

    // Si el usuario es Conductor, transmite su posición GPS de forma segura a Firebase
    var transmitiendoUbicacion by remember { mutableStateOf(false) }
    LaunchedEffect(transmitiendoUbicacion, esConductor) {
        if (esConductor) {
            if (transmitiendoUbicacion) {
                android.util.Log.d("LOCATION_SYNC", "Driver activa ubicación: $nombreUsuario")
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
                    delay(5000.milliseconds)
                }
            } else {
                android.util.Log.d("LOCATION_SYNC", "Driver desactiva ubicación: $nombreUsuario")
                scope.launch {
                    syncManager.broadcastConductorLocation(
                        conductorId = nombreUsuario,
                        nombre = "Conductor $nombreUsuario",
                        ruta = "Línea L1",
                        lat = ubicacionGpsPoint?.latitude() ?: centroActual.latitude(),
                        lng = ubicacionGpsPoint?.longitude() ?: centroActual.longitude(),
                        activo = false
                    )
                    android.util.Log.d("LOCATION_SYNC", "Backend actualizado locationEnabled=false driver=$nombreUsuario")
                }
            }
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

    if (esPantallaAncha) {
        // 🖥️ VISTA DEDICADA Y ADAPTATIVA PARA TABLET (IGUAL A LA FOTO DE REFERENCIA)
        Box(modifier = Modifier.fillMaxSize()) {
            // MAPA A PANTALLA COMPLETA
            MapaOptimizadoContainer(
                modifier = Modifier.fillMaxSize(),
                paradas = paradasFiltradas,
                conductores = conductoresActivos,
                rutasOsm = rutasOsmReal,
                centroPoint = centroActual,
                ubicacionCentradaPoint = ubicacionGpsPoint,
                onParadaSelect = { parada ->
                    paradaSeleccionada = parada
                }
            )

            // CAJA FLOTANTE INFERIOR IZQUIERDA (BUSCADOR Y FILTROS DEL RECUADRO MORADO)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
                    .widthIn(max = 380.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = surfaceColor.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, onBackground.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // BUSCADOR CON ICONO DE LUPA
                    OutlinedTextField(
                        value = busquedaTexto,
                        onValueChange = { busquedaTexto = it },
                        placeholder = { Text("Buscar línea o parada...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = if (busquedaTexto.isNotEmpty()) {
                            {
                                IconButton(onClick = { busquedaTexto = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                                }
                            }
                        } else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // CHIPS DE FILTRO RÁPIDO HORIZONTALES ("Todas", "Directos", "Incidencias")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Todas", "Directos", "Incidencias").forEach { filtro ->
                            val seleccionado = filtroActivo == filtro
                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    filtroActivo = filtro
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (seleccionado) Color(0xFFD35400) else Color(0xFFBDC3C7).copy(alpha = 0.4f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = filtro,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (seleccionado) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }


            // INSIGNIA FLOTANTE DE PERFIL EN EL MAPA (TopStart)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
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
                            color = MaterialTheme.colorScheme.onSurface
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

            // CONTROLES FLOTANTES EN LA ESQUINA INFERIOR DERECHA (RECALCULAR Y CENTRAR GPS)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // RECALCULAR / ACTUALIZAR
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
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Autorenew, contentDescription = null, tint = Color(0xFFD35400), modifier = Modifier.size(24.dp))
                    }
                }

                // CENTRAR GPS
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        obtenerUbicacionGpsReal { realPoint ->
                            ubicacionGpsPoint = realPoint
                            Toast.makeText(context, "🎯 Mapa centrado en tu posición GPS", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFD35400),
                    shadowElevation = 8.dp
                ) {
                    Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    } else {
        // 📱 DISEÑO VERTICAL TRADICIONAL PARA TELÉFONOS
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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

                if (modoOffline) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF39C12).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFF39C12))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = Color(0xFFF39C12), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Modo Offline Activo", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFF39C12))
                                Text("Ahorrando datos móviles. Actualización en tiempo real pausada.", fontSize = 11.sp, color = onSurface.copy(alpha = 0.7f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // MAPA INTERACTIVO NATIVO EN MAPBOX COMPOSE V11
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .semantics {
                            contentDescription = "Mapa interactivo nativo Mapbox con líneas de transporte y ubicaciones"
                        }
                ) {
                    if (isMapVisible) {
                        MapaOptimizadoContainer(
                            modifier = Modifier.fillMaxSize(),
                            paradas = paradasFiltradas,
                            conductores = conductoresActivos,
                            rutasOsm = rutasOsmReal,
                            centroPoint = centroActual,
                            ubicacionCentradaPoint = ubicacionGpsPoint,
                            onParadaSelect = { parada ->
                                paradaSeleccionada = parada
                            }
                        )
                    }

                    // Capa superpuesta (FadeOut)
                    if (!mapReady && !isLeavingScreen) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Actualizando GPS...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else if (!isMapVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }

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
                        if (esConductor) {
                            Surface(
                                onClick = {
                                    ejecutarVibracionHaptica(context, 60L)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    transmitiendoUbicacion = !transmitiendoUbicacion
                                    Toast.makeText(
                                        context,
                                        if (transmitiendoUbicacion) "📡 Transmitiendo ubicación GPS en tiempo real..." else "⏸️ Transmisión en vivo pausada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (transmitiendoUbicacion) Color(0xFFE74C3C) else Color(0xFF2ECC71),
                                shadowElevation = 6.dp,
                                modifier = Modifier.semantics {
                                    role = Role.Button
                                    contentDescription = "Transmitir ubicación en vivo a pasajeros"
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (transmitiendoUbicacion) Icons.Default.Sensors else Icons.Default.SensorsOff,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (transmitiendoUbicacion) "EN VIVO" else "Transmitir",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

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
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
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
                            shadowElevation = 6.dp
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
                                paradaSeleccionada = null
                                navController.navigate("horario") {
                                    popUpTo("principal") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
                                paradaSeleccionada = null
                                navController.navigate("alertas") {
                                    popUpTo("principal") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
                                scope.launch {
                                    val userId = nombreUsuario
                                    syncManager.syncRutaFrecuenteToCloud(userId, parada.nombre, "Líneas: ${parada.lineas.joinToString(", ")}")
                                }
                                Toast.makeText(context, "⭐ Parada '${parada.nombre}' guardada en tus favoritas", Toast.LENGTH_SHORT).show()
                                paradaSeleccionada = null
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
    }

@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapaOptimizadoContainer(
    modifier: Modifier = Modifier,
    paradas: List<ParadaMapa>,
    conductores: List<ConductorUbicacion> = emptyList(),
    rutasOsm: List<List<Point>> = emptyList(),
    centroPoint: Point,
    ubicacionCentradaPoint: Point? = null,
    onParadaSelect: (ParadaMapa) -> Unit
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
        paradas.map { it.ubicacion }
    }

    val rutaAzulPoints = remember(paradas) {
        paradas.map { it.ubicacion }
    }

    val conductoresAdaptativos = remember(conductores, centroPoint) {
        conductores
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
                        v.performClick()
                        false
                    }
                } catch (_: Exception) { }
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

            // Rutas reales de OpenStreetMap (Puebla) en tiempo real (100% Gratis)
            rutasOsm.forEach { puntos ->
                PolylineAnnotation(
                    points = puntos,
                    lineColorString = "#16A085",
                    lineWidth = 3.5
                )
            }

            key(paradas) {
                paradas.forEach { parada ->
                    key(parada.nombre) {
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
                }
            }

            key(conductoresAdaptativos) {
                conductoresAdaptativos.forEach { conductor ->
                    key(conductor.id, conductor.lat, conductor.lng) {
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
    }
}
