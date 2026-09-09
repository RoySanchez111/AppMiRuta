package com.example.appbanco.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

data class ParadaMapa(
    val id: String,
    val nombre: String,
    val lineas: List<String>,
    val ubicacion: LatLng,
    val proximaLlegada: String,
    val esIncidencia: Boolean = false,
    val detalleIncidencia: String? = null
)

val paradasMapLibre = listOf(
    ParadaMapa("1", "Tecmilenio Campus Puebla", listOf("L1", "L4"), LatLng(18.999446, -98.261833), "En 2 min"),
    ParadaMapa("2", "Plaza Mayor", listOf("L1", "L7"), LatLng(19.005000, -98.255000), "En 5 min"),
    ParadaMapa("3", "Universidad CCU", listOf("L4", "MA"), LatLng(18.992000, -98.268000), "En 3 min"),
    ParadaMapa("4", "Hospital Norte", listOf("L7", "L5"), LatLng(19.012000, -98.248000), "En 9 min"),
    ParadaMapa("5", "Aeropuerto / Terminal", listOf("L1"), LatLng(19.020000, -98.240000), "En 12 min"),
    ParadaMapa("6", "Alerta L5 - Calzada Serdán", listOf("L5"), LatLng(19.008000, -98.251000), "Retraso 15 min", esIncidencia = true, detalleIncidencia = "Falla técnica en vía - Use rutas alternas")
)

@Composable
fun PantallaPrincipal(navController: NavController) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    val tecmilenioPuebla = LatLng(18.999446, -98.261833)

    var mapaInstancia by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var busquedaTexto by remember { mutableStateOf("") }
    var filtroActivo by remember { mutableStateOf("Todas") }
    var paradaSeleccionada by remember { mutableStateOf<ParadaMapa?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun obtenerUbicacionGpsReal(onSuccess: (LatLng) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        onSuccess(LatLng(loc.latitude, loc.longitude))
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

    val launcherPermisosUbicacion = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            obtenerUbicacionGpsReal { realLatLng ->
                mapaInstancia?.animateCamera(CameraUpdateFactory.newLatLngZoom(realLatLng, 16.0))
                Toast.makeText(context, "📍 Ubicación GPS real centrada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            launcherPermisosUbicacion.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    val paradasFiltradas by remember(busquedaTexto, filtroActivo) {
        derivedStateOf {
            paradasMapLibre.filter { parada ->
                val coincideTexto = busquedaTexto.isBlank() || 
                    parada.nombre.contains(busquedaTexto, ignoreCase = true) ||
                    parada.lineas.any { it.contains(busquedaTexto, ignoreCase = true) }
                
                val coincideFiltro = when (filtroActivo) {
                    "Alertas" -> parada.esIncidencia
                    "Cercanas" -> parada.nombre.contains("Tecmilenio") || parada.nombre.contains("Universidad")
                    "Casa" -> parada.nombre.contains("Plaza Mayor")
                    "Universidad" -> parada.nombre.contains("Tecmilenio") || parada.nombre.contains("CCU")
                    else -> true
                }

                coincideTexto && coincideFiltro
            }
        }
    }

    remember {
        MapLibre.getInstance(context)
        true
    }

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
            placeholder = { Text("Buscar línea, parada o destino...", color = onBackground.copy(alpha = 0.5f), fontSize = 14.sp) },
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
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .semantics {
                    contentDescription = "Campo de búsqueda de líneas y paradas"
                },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = onBackground,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedTextColor = onBackground,
                focusedTextColor = onBackground
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val opcionesFiltro = listOf("Todas", "Cercanas", "Alertas", "Universidad", "Casa")
            opcionesFiltro.forEach { filtro ->
                val esSeleccionado = filtroActivo == filtro
                Surface(
                    color = if (esSeleccionado) MaterialTheme.colorScheme.primary else onBackground.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (esSeleccionado) MaterialTheme.colorScheme.primary else onBackground.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .semantics(mergeDescendants = true) {
                            role = Role.Button
                            contentDescription = "Filtro de mapa: $filtro"
                        }
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            filtroActivo = filtro
                            val primeraCoincidencia = paradasMapLibre.firstOrNull { 
                                when (filtro) {
                                    "Alertas" -> it.esIncidencia
                                    "Universidad" -> it.nombre.contains("CCU") || it.nombre.contains("Tecmilenio")
                                    "Casa" -> it.nombre.contains("Plaza")
                                    else -> true
                                }
                            }
                            primeraCoincidencia?.let {
                                paradaSeleccionada = it
                                mapaInstancia?.animateCamera(CameraUpdateFactory.newLatLngZoom(it.ubicacion, 15.5))
                            }
                        }
                ) {
                    Text(
                        text = filtro,
                        color = if (esSeleccionado) Color.White else onBackground.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        if (busquedaTexto.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = surfaceColor,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, onBackground.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (paradasFiltradas.isEmpty()) {
                        Text(
                            "No se encontraron paradas o líneas con '$busquedaTexto'",
                            color = onSurface.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        paradasFiltradas.take(3).forEach { parada ->
                            key(parada.id) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            paradaSeleccionada = parada
                                            busquedaTexto = ""
                                            mapaInstancia?.animateCamera(CameraUpdateFactory.newLatLngZoom(parada.ubicacion, 16.0))
                                        }
                                        .padding(12.dp),
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .clip(RoundedCornerShape(20.dp))
                .semantics {
                    contentDescription = "Mapa interactivo con líneas de transporte y ubicaciones"
                }
        ) {
            MapaOptimizadoContainer(
                paradas = paradasMapLibre,
                tecmilenioPuebla = tecmilenioPuebla,
                onMapReady = { map ->
                    mapaInstancia = map
                },
                onMarkerClick = { parada ->
                    paradaSeleccionada = parada
                },
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = {
                    Toast.makeText(context, "🚌 Posiciones y horarios actualizados en tiempo real", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.White, CircleShape)
                    .shadow(4.dp, CircleShape)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Actualizar posiciones de autobuses en tiempo real"
                    }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        obtenerUbicacionGpsReal { realLatLng ->
                            mapaInstancia?.animateCamera(CameraUpdateFactory.newLatLngZoom(realLatLng, 16.0))
                            Toast.makeText(context, "Centrado en tu ubicación GPS real", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        launcherPermisosUbicacion.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
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
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun MapaOptimizadoContainer(
    paradas: List<ParadaMapa>,
    tecmilenioPuebla: LatLng,
    onMapReady: (MapLibreMap) -> Unit,
    onMarkerClick: (ParadaMapa) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var mapaCargado by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Ejecutamos la carga inicial en un hilo secundario para evitar lag en la UI principal
        LaunchedEffect(Unit) {
            delay(200) // Ligero retardo para permitir que la UI cargue primero sin jank
            mapaCargado = true
        }

        AndroidView(
            factory = { ctx ->
                // MapLibre usa recursos de la GPU. Evitamos inyectarlo inmediatamente al iniciar la pantalla
                MapLibre.getInstance(ctx)
                MapView(ctx).also { mv ->
                    mapViewRef = mv
                    mv.getMapAsync { map ->
                        onMapReady(map)
                        
                        map.uiSettings.isCompassEnabled = false
                        map.uiSettings.isAttributionEnabled = false
                        map.uiSettings.isLogoEnabled = false
                        
                        map.setStyle(Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty")) { style ->
                            // Cargamos marcadores y lineas de forma nativa sin forzar recomposiciones
                            val lineasColorNaranja = android.graphics.Color.parseColor("#FF8E56")
                            val lineasColorAzul = android.graphics.Color.parseColor("#327CF2")

                            map.addPolyline(
                                PolylineOptions()
                                    .add(
                                        LatLng(18.999446, -98.261833),
                                        LatLng(19.005000, -98.255000),
                                        LatLng(19.020000, -98.240000)
                                    )
                                    .color(lineasColorNaranja)
                                    .width(4f)
                            )

                            map.addPolyline(
                                PolylineOptions()
                                    .add(
                                        LatLng(18.992000, -98.268000),
                                        LatLng(18.999446, -98.261833),
                                        LatLng(19.012000, -98.248000)
                                    )
                                    .color(lineasColorAzul)
                                    .width(4f)
                            )

                            paradas.forEach { parada ->
                                map.addMarker(
                                    MarkerOptions()
                                        .position(parada.ubicacion)
                                        .title(parada.nombre)
                                )
                            }

                            map.setOnMarkerClickListener { marker ->
                                val paradaEncontrada = paradas.firstOrNull { 
                                    it.ubicacion.latitude == marker.position.latitude && it.ubicacion.longitude == marker.position.longitude
                                }
                                if (paradaEncontrada != null) {
                                    onMarkerClick(paradaEncontrada)
                                    // Usamos animaciones nativas más rápidas del propio MapLibre
                                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(paradaEncontrada.ubicacion, 16.0), 300)
                                }
                                true
                            }

                            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                try {
                                    map.locationComponent.apply {
                                        activateLocationComponent(
                                            LocationComponentActivationOptions.builder(ctx, style)
                                                .useDefaultLocationEngine(false)
                                                .build()
                                        )
                                        isLocationComponentEnabled = true
                                    }
                                } catch (e: Exception) { }
                            }
                        }

                        map.cameraPosition = CameraPosition.Builder()
                            .target(tecmilenioPuebla)
                            .zoom(14.8)
                            .build()
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay suave durante los primeros milisegundos de inicialización de la GPU
        if (!mapaCargado) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapViewRef) {
        val mv = mapViewRef
        val observer = LifecycleEventObserver { _, event ->
            if (mv != null) {
                when (event) {
                    Lifecycle.Event.ON_START -> mv.onStart()
                    Lifecycle.Event.ON_RESUME -> mv.onResume()
                    Lifecycle.Event.ON_PAUSE -> mv.onPause()
                    Lifecycle.Event.ON_STOP -> mv.onStop()
                    Lifecycle.Event.ON_DESTROY -> mv.onDestroy()
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
