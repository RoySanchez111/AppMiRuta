package com.example.appbanco.ui.screens

import com.example.appbanco.logic.SessionManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appbanco.data.database.UserDao
import androidx.navigation.NavController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.example.appbanco.data.alertasSimuladas
import com.example.appbanco.ui.components.*
import com.example.appbanco.ui.viewmodel.MainViewModel
import com.example.appbanco.ui.viewmodel.RutaFrecuenteItem
import com.example.appbanco.logic.LineaHorario
import com.example.appbanco.logic.ServicioHorarios
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import android.widget.Toast
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.location.LocationComponentActivationOptions
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import com.google.android.gms.location.LocationServices
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale

// MODELO DE DATOS PARA PARADAS EN EL MAPA
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

    // Coordenadas iniciales (Tecmilenio Campus Puebla)
    val tecmilenioPuebla = LatLng(18.999446, -98.261833)

    var mapaInstancia by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var busquedaTexto by remember { mutableStateOf("") }
    var filtroActivo by remember { mutableStateOf("Todas") }
    var paradaSeleccionada by remember { mutableStateOf<ParadaMapa?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Obtenedor de ubicación GPS real del dispositivo
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

    // Solicitante de permisos de ubicación nativos de Android
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
        // Solicitar permisos nativos de GPS al abrir el mapa si no están concedidos
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            launcherPermisosUbicacion.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    // Paradas filtradas optimizadas con derivedStateOf
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

    // Inicializar MapLibre
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
        // BUSCADOR REAL
        OutlinedTextField(
            value = busquedaTexto,
            onValueChange = { busquedaTexto = it },
            placeholder = { Text("Buscar línea, parada o destino...", color = onBackground.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = onBackground.copy(alpha = 0.5f)) },
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
                    contentDescription = "Campo de búsqueda de líneas y paradas"
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

        Spacer(modifier = Modifier.height(12.dp))

        // CHIPS DE FILTRO RÁPIDO
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
                            filtroActivo = filtro
                            // Si selecciona un filtro específico, mover la cámara a la primera parada coincidente
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
                        text = if (filtro == "Alertas") "⚠️ Alertas" else if (filtro == "Todas") "🚌 Todas las Líneas" else filtro,
                        color = if (esSeleccionado) Color.White else onBackground.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // RESULTADOS DE BÚSQUEDA RÁPIDA (SUGERENCIAS)
        if (busquedaTexto.isNotBlank()) {
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

        // MAPA REAL MAPLIBRE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .clip(RoundedCornerShape(20.dp))
                .semantics {
                    contentDescription = "Mapa interactivo con líneas de transporte y ubicaciones"
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    // Asegurar que MapLibre esté inicializado antes de instanciar MapView
                    MapLibre.getInstance(ctx)
                    MapView(ctx).also { mv ->
                        mapViewRef = mv
                        mv.getMapAsync { map ->
                            mapaInstancia = map
                            
                            // Estilo de OpenFreeMap (Libre, nítido y rápido)
                            map.setStyle(Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty")) { style ->
                                
                                // Trazar Líneas de Ruta (Polylines)
                                map.addPolyline(
                                    PolylineOptions()
                                        .add(
                                            LatLng(18.999446, -98.261833), // Tecmilenio
                                            LatLng(19.005000, -98.255000), // Plaza Mayor
                                            LatLng(19.020000, -98.240000)  // Aeropuerto
                                        )
                                        .color(android.graphics.Color.parseColor("#FF8E56"))
                                        .width(5f)
                                )

                                map.addPolyline(
                                    PolylineOptions()
                                        .add(
                                            LatLng(18.992000, -98.268000), // Universidad CCU
                                            LatLng(18.999446, -98.261833), // Tecmilenio
                                            LatLng(19.012000, -98.248000)  // Hospital Norte
                                        )
                                        .color(android.graphics.Color.parseColor("#327CF2"))
                                        .width(5f)
                                )

                                // Añadir Marcadores de Paradas en el Mapa
                                paradasMapLibre.forEach { parada ->
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(parada.ubicacion)
                                            .title(parada.nombre)
                                            .snippet(if (parada.esIncidencia) "⚠️ ${parada.proximaLlegada}" else "Líneas: ${parada.lineas.joinToString()} • ${parada.proximaLlegada}")
                                    )
                                }

                                // Manejador de Clics en los Marcadores del Mapa
                                map.setOnMarkerClickListener { marker ->
                                    val paradaEncontrada = paradasMapLibre.firstOrNull { 
                                        it.nombre == marker.title || (it.ubicacion.latitude == marker.position.latitude && it.ubicacion.longitude == marker.position.longitude)
                                    }
                                    if (paradaEncontrada != null) {
                                        paradaSeleccionada = paradaEncontrada
                                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(paradaEncontrada.ubicacion, 16.0))
                                    }
                                    true
                                }

                                // Activar componente de ubicación si hay permisos (de forma segura dentro del estilo cargado)
                                if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    try {
                                        map.locationComponent.apply {
                                            activateLocationComponent(
                                                LocationComponentActivationOptions.builder(ctx, style).build()
                                            )
                                            isLocationComponentEnabled = true
                                        }
                                    } catch (e: Exception) {
                                        // Manejo silencioso de excepciones de ubicación en emulador
                                    }
                                }
                            }

                            // Configuración inicial de cámara
                            map.cameraPosition = CameraPosition.Builder()
                                .target(tecmilenioPuebla)
                                .zoom(14.8)
                                .build()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Propagación de ciclo de vida del Mapa para evitar fugas de memoria y mantener fluidez
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

            // BOTÓN REFRESCAR / SIMULAR AUTOBÚS EN TIEMPO REAL
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

            // BOTÓN CENTRAR EN MI UBICACIÓN GPS REAL
            IconButton(
                onClick = {
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

        // CARD INFORMATIVA DE PARADA SELECCIONADA EN EL MAPA
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
                border = BorderStroke(1.dp, if (parada.esIncidencia) Color(0xFFC0392B) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(if (parada.esIncidencia) Color(0xFFC0392B) else MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (parada.esIncidencia) Icons.Default.Warning else Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(parada.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurface)
                                Text("Próximo autobús: ${parada.proximaLlegada}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        IconButton(onClick = { paradaSeleccionada = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar detalles de parada", tint = onSurface.copy(alpha = 0.5f))
                        }
                    }

                    if (parada.esIncidencia && parada.detalleIncidencia != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFFADBD8),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ ${parada.detalleIncidencia}",
                                color = Color(0xFF922B21),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("horario") },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Ver horarios de ${parada.nombre}"
                                },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ver Horarios", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHorarios(navController: NavController, viewModel: MainViewModel) {
    val servicioHorarios = viewModel.servicioHorarios
    val onBackground = MaterialTheme.colorScheme.onBackground
    val onSurface = MaterialTheme.colorScheme.onSurface

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
            .padding(16.dp)
    ) {
        // BUSCADOR REAL
        OutlinedTextField(
            value = busquedaTexto,
            onValueChange = { busquedaTexto = it },
            placeholder = { Text("Buscar línea, parada o destino...", color = onBackground.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = onBackground.copy(alpha = 0.5f)) },
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
                .background(onBackground.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .semantics {
                    contentDescription = "Campo de búsqueda para horarios de línea o parada"
                },
            shape = RoundedCornerShape(12.dp),
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

        Spacer(modifier = Modifier.height(20.dp))

        // FILTROS DE FECHA Y TIEMPO REAL
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val etiquetaAhora = if (fechaOffsetDias == 0) "Ahora - $horaActualFormateada" else "Hoy"
            Surface(
                color = if (fechaOffsetDias == 0) Color(0xFFFF3B30) else onBackground.copy(alpha = 0.05f), 
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (fechaOffsetDias == 0) Color(0xFFFF3B30) else onBackground.copy(alpha = 0.1f)),
                modifier = Modifier
                    .semantics(mergeDescendants = true) {
                        role = Role.Button
                        contentDescription = "Filtro de fecha: $etiquetaAhora"
                    }
                    .clickable { fechaOffsetDias = 0 }
            ) {
                Text(
                    text = etiquetaAhora, 
                    color = if (fechaOffsetDias == 0) Color.White else onBackground.copy(alpha = 0.7f), 
                    fontSize = 12.sp, 
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), 
                    fontWeight = FontWeight.Bold
                )
            }

            listOf(Pair(1, "Mañana"), Pair(2, "Próximos días")).forEach { (offset, texto) -> 
                val esSeleccionado = fechaOffsetDias == offset
                Surface(
                    color = if (esSeleccionado) Color(0xFFFF3B30) else onBackground.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if (esSeleccionado) Color(0xFFFF3B30) else onBackground.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .semantics(mergeDescendants = true) {
                            role = Role.Button
                            contentDescription = "Filtro de fecha: $texto"
                        }
                        .clickable { fechaOffsetDias = offset }
                ) {
                    Text(
                        text = texto, 
                        color = if (esSeleccionado) Color.White else onBackground.copy(alpha = 0.7f), 
                        fontSize = 12.sp, 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics { heading() }
        ) {
            Icon(Icons.Default.DirectionsBus, null, tint = onBackground.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("LÍNEAS DE TRANSPORTE DISPONIBLES", color = onBackground.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (lineasFiltradas.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = onBackground.copy(alpha = 0.03f)
            ) {
                Text(
                    text = "No se encontraron líneas de transporte con el nombre '$busquedaTexto'",
                    color = onBackground.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            lineasFiltradas.forEach { linea ->
                TarjetaLineaHorario(
                    linea = linea,
                    proximaSalida = servicioHorarios.calcularProximasSalidas(linea.frecuenciaMinutos, 1, fechaOffsetDias).firstOrNull() ?: "--:--",
                    onClick = { lineaSeleccionada = linea }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Próximas salidas en Plaza Mayor (En Vivo)", 
            color = onBackground.copy(alpha = 0.6f), 
            fontSize = 13.sp, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            proximasSalidasPlazaMayor.forEachIndexed { index, hora ->
                val esInmediata = index == 0 && fechaOffsetDias == 0
                Surface(
                    color = if (esInmediata) Color(0xFF389338) else onBackground.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if (esInmediata) Color(0xFF389338) else onBackground.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "Próxima salida a las $hora ${if (esInmediata) "inmediata" else ""}"
                    }
                ) {
                    Text(
                        text = if (esInmediata) "$hora - ahora" else hora, 
                        color = if (esInmediata) Color.White else onBackground.copy(alpha = 0.8f), 
                        fontSize = 12.sp, 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Volver a la pantalla anterior"
                },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2697B5), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver", fontWeight = FontWeight.Bold)
        }
    }

    // MODAL DE DETALLE COMPLETO DE LÍNEA Y HORARIOS REALES
    if (lineaSeleccionada != null) {
        val linea = lineaSeleccionada!!
        val salidasCalculadas = servicioHorarios.calcularProximasSalidas(linea.frecuenciaMinutos, 6, fechaOffsetDias)

        AlertDialog(
            onDismissRequest = { lineaSeleccionada = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).background(linea.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(linea.codigo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(linea.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(linea.recorrido, fontSize = 12.sp, color = onSurface.copy(alpha = 0.6f))
                    }
                }
            },
            text = {
                Column {
                    Surface(
                        color = if (linea.estadoServicio == "Operación Normal") Color(0xFFE8F5E9) else Color(0xFFFADBD8),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Estado: ${linea.estadoServicio}",
                            color = if (linea.estadoServicio == "Operación Normal") Color(0xFF2E7D32) else Color(0xFFC0392B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
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
                Button(onClick = { lineaSeleccionada = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.05f))
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

// MODELO DE DATOS PARA ALERTAS
data class Incidencia(
    val tipo: String,
    val ruta: String,
    val titulo: String,
    val descripcion: String,
    val tiempo: String,
    val colorEtiqueta: Color,
    val colorRuta: Color
)

@Composable
fun PantallaAlertas(navController: NavController, viewModel: MainViewModel) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = Color(0xFF282869)

    val listaIncidencias = viewModel.listaIncidencias
    var mostrarDialogo by remember { mutableStateOf(false) }
    var mostrarExito by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            listaIncidencias.forEach { incidencia ->
                TarjetaAlerta(incidencia)
                Spacer(modifier = Modifier.height(16.dp))
            }
            Spacer(modifier = Modifier.height(100.dp))
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

    if (mostrarDialogo) {
        DialogoReporte(
            onDismiss = { mostrarDialogo = false },
            onConfirm = { nuevaRuta, nuevaDescripcion ->
                viewModel.agregarIncidencia(
                    Incidencia(
                        tipo = "Reporte de Usuario",
                        ruta = nuevaRuta.take(2).uppercase(),
                        titulo = "Incidencia en Ruta $nuevaRuta",
                        descripcion = nuevaDescripcion,
                        tiempo = "Hace un momento",
                        colorEtiqueta = Color(0xFF3498DB),
                        colorRuta = Color.Gray
                    )
                )
                mostrarDialogo = false

                mostrarExito = true
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

@Composable
fun TarjetaAlerta(incidencia: Incidencia) {

    // Controla si la información está desplegada
    var expandida by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "Alerta ${incidencia.tipo} en ruta ${incidencia.ruta}: ${incidencia.titulo}. ${incidencia.descripcion}. ${incidencia.tiempo}. ${if (expandida) "Toca para contraer" else "Toca para expandir"}"
            }
            .clickable {
                expandida = !expandida
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Parte que siempre permanece visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = incidencia.colorEtiqueta,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = incidencia.tipo,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = incidencia.colorRuta,
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = incidencia.ruta,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = if (expandida) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = if (expandida) {
                            "Contraer información"
                        } else {
                            "Mostrar información"
                        },
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Esta información solamente aparece al tocar la tarjeta
            if (expandida) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = incidencia.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = incidencia.descripcion,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = incidencia.tiempo,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
@Composable
fun DialogoReporte(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var ruta by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    var errorRuta by remember { mutableStateOf(false) }
    var errorDescripcion by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                "Reportar nueva incidencia",
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Column {

                // CAMPO RUTA
                OutlinedTextField(
                    value = ruta,
                    onValueChange = {
                        ruta = it
                        errorRuta = false
                    },
                    label = {
                        Text("Línea/Ruta (Ej. L4)")
                    },
                    singleLine = true,
                    isError = errorRuta,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorRuta) {
                    Text(
                        text = "Debes ingresar una línea o ruta.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                // CAMPO DESCRIPCIÓN
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                        errorDescripcion = false
                    },
                    label = {
                        Text("Descripción del problema")
                    },
                    isError = errorDescripcion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                if (errorDescripcion) {
                    Text(
                        text = "Debes ingresar una descripción.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {

                    // Comprobamos cada campo
                    errorRuta = ruta.isBlank()
                    errorDescripcion = descripcion.isBlank()

                    // Si ninguno tiene error, enviamos
                    if (!errorRuta && !errorDescripcion) {
                        onConfirm(ruta, descripcion)
                    }
                }
            ) {
                Text("Reportar")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AnimacionReporteExitoso(
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {

        // Tiempo que permanece la confirmación en pantalla
        delay(1000)

        // Después de 2 segundos regresa a la pantalla normal
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.35f)
            )
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = "¡Reporte enviado! La incidencia fue registrada correctamente."
            },
        contentAlignment = Alignment.Center
    ) {

        // TARJETA DE CONFIRMACIÓN
        Surface(
            modifier = Modifier
                .width(280.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {

            Column(
                modifier = Modifier
                    .padding(
                        horizontal = 30.dp,
                        vertical = 28.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // CÍRCULO CON LA PALOMITA
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            color = Color(0xFFE8F5E9),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "✓",
                        fontSize = 55.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Text(
                    text = "¡Reporte enviado!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "La incidencia fue registrada correctamente.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PantallaCuenta(
    sessionManager: SessionManager, 
    navController: NavController, 
    viewModel: MainViewModel,
    userDao: UserDao
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onBackground = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    val context = LocalContext.current
    var modoOffline by remember { mutableStateOf(true) }
    var mostrarDialogoTema by remember { mutableStateOf(false) }
    var mostrarDialogoNombre by remember { mutableStateOf(false) }
    var mostrarDialogoPrivacidad by remember { mutableStateOf(false) }
    var mostrarDialogoAyuda by remember { mutableStateOf(false) }
    var mostrarDialogoAgregarRuta by remember { mutableStateOf(false) }
    var rutaSeleccionadaOpciones by remember { mutableStateOf<RutaFrecuenteItem?>(null) }
    var efectosPantallaActivados by remember { mutableStateOf(true) }
    var talkbackActivado by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val usuarioActual = viewModel.usuarioActual.value
    val inicialUsuario = if (usuarioActual.isNotBlank()) usuarioActual.take(1).uppercase() else "U"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(surfaceColor)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Usuario $usuarioActual"
                }, 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(onSurface.copy(alpha = 0.1f), CircleShape), 
                contentAlignment = Alignment.Center
            ) {
                Text(inicialUsuario, fontWeight = FontWeight.Bold, color = onSurface, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(usuarioActual, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = onSurface)
                Text("Usuario registrado", fontSize = 13.sp, color = onSurface.copy(alpha = 0.6f))
            }
            IconButton(
                onClick = { mostrarDialogoNombre = true },
                modifier = Modifier.semantics {
                    role = Role.Button
                    contentDescription = "Editar nombre de usuario"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar nombre",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECCIÓN RUTAS FRECUENTES DINÁMICAS
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Rutas frecuentes", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                TextButton(
                    onClick = { mostrarDialogoAgregarRuta = true },
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "Agregar nueva ruta frecuente"
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MOSTRAR RUTAS EN PAREJAS DE 2 POR FILA
            viewModel.listaRutasFrecuentes.chunked(2).forEach { parRutas ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    parRutas.forEach { ruta ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(84.dp)
                                .semantics(mergeDescendants = true) {
                                    role = Role.Button
                                    contentDescription = "Ruta frecuente ${ruta.nombre}, ${ruta.ubicacion}"
                                }
                                .clickable {
                                    rutaSeleccionadaOpciones = ruta
                                }, 
                            shape = RoundedCornerShape(16.dp), 
                            color = ruta.color
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp), 
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) { 
                                    Icon(ruta.icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(ruta.nombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) 
                                }
                                Text(ruta.ubicacion, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                    if (parRutas.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Text(
                "Modo Offline", 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Bold, 
                color = onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        role = Role.Switch
                        contentDescription = "Modo Offline, Habilitar descarga de mapas: ${if (modoOffline) "Activado" else "Desactivado"}"
                    }, 
                shape = RoundedCornerShape(18.dp), 
                color = onSurface.copy(alpha = 0.05f)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Habilitar descarga de mapas", fontSize = 13.sp, color = onSurface.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
                    Switch(checked = modoOffline, onCheckedChange = { modoOffline = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFEB30F)))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), shape = RoundedCornerShape(24.dp), color = onBackground.copy(alpha = 0.03f)) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OpcionCuenta("Configuración", onClick = { mostrarDialogoNombre = true })
                OpcionCuenta(
                    texto = "Tema de la APP", 
                    subtexto = viewModel.modoTema.value,
                    onClick = { mostrarDialogoTema = true }
                )
                OpcionCuenta("Privacidad y Seguridad", onClick = { mostrarDialogoPrivacidad = true })
                OpcionCuenta(
                    texto = "Ayuda y Soporte", 
                    subtexto = if (efectosPantallaActivados) "Efectos ON" else "Efectos OFF",
                    onClick = { mostrarDialogoAyuda = true }
                )
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
                .height(52.dp)
                .padding(horizontal = 4.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Cerrar sesión"
                },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (mostrarDialogoAgregarRuta) {
        DialogoAgregarRutaFrecuente(
            onDismiss = { mostrarDialogoAgregarRuta = false },
            onConfirmar = { nuevaRuta ->
                viewModel.agregarRutaFrecuente(nuevaRuta)
                mostrarDialogoAgregarRuta = false
            }
        )
    }

    if (rutaSeleccionadaOpciones != null) {
        val ruta = rutaSeleccionadaOpciones!!
        DialogoOpcionesRutaFrecuente(
            ruta = ruta,
            onDismiss = { rutaSeleccionadaOpciones = null },
            onVerEnMapa = {
                navController.navigate("principal")
            },
            onEliminar = {
                viewModel.eliminarRutaFrecuente(ruta.id)
                Toast.makeText(context, "Ruta '${ruta.nombre}' eliminada", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (mostrarDialogoTema) {
        DialogoTema(
            temaActual = viewModel.modoTema.value,
            onDismiss = { mostrarDialogoTema = false },
            onSeleccionarTema = { nuevoTema ->
                viewModel.cambiarTema(nuevoTema)
                mostrarDialogoTema = false
            }
        )
    }

    if (mostrarDialogoNombre) {
        DialogoEditarNombre(
            nombreActual = usuarioActual,
            onDismiss = { mostrarDialogoNombre = false },
            onConfirmar = { nuevoNombre ->
                viewModel.actualizarNombreUsuario(userDao, nuevoNombre) { _ ->
                    mostrarDialogoNombre = false
                }
            }
        )
    }

    if (mostrarDialogoPrivacidad) {
        DialogoPrivacidadEULA(onDismiss = { mostrarDialogoPrivacidad = false })
    }

    if (mostrarDialogoAyuda) {
        DialogoAyudaYSoporte(
            efectosActivados = efectosPantallaActivados,
            onToggleEfectos = { efectosPantallaActivados = it },
            talkbackActivado = talkbackActivado,
            onToggleTalkback = { talkbackActivado = it },
            onDismiss = { mostrarDialogoAyuda = false }
        )
    }
}

@Composable
fun DialogoTema(
    temaActual: String,
    onDismiss: () -> Unit,
    onSeleccionarTema: (String) -> Unit
) {
    val opciones = listOf("Degradados", "Claro", "Oscuro")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleccionar Tema de la APP",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                opciones.forEach { opcion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .semantics(mergeDescendants = true) {
                                role = Role.RadioButton
                                contentDescription = if (opcion == "Degradados") "Tema Degradados por defecto" else "Tema $opcion"
                            }
                            .clickable { onSeleccionarTema(opcion) }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (opcion == temaActual),
                            onClick = { onSeleccionarTema(opcion) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (opcion == "Degradados") "Degradados (por defecto)" else opcion,
                            fontSize = 15.sp,
                            fontWeight = if (opcion == temaActual) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun DialogoEditarNombre(
    nombreActual: String,
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit
) {
    var nuevoNombre by remember { mutableStateOf(nombreActual) }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Nombre de Usuario",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Column {
                Text(
                    text = "Escribe tu nuevo nombre para actualizar la base de datos y la app:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = nuevoNombre,
                    onValueChange = {
                        nuevoNombre = it
                        errorMsg = ""
                    },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Campo de nuevo nombre de usuario" }
                )
                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nuevoNombre.isNotBlank()) {
                        onConfirmar(nuevoNombre.trim())
                    } else {
                        errorMsg = "El nombre no puede estar vacío"
                    }
                }
            ) {
                Text("Guardar")
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
fun DialogoPrivacidadEULA(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Términos de Licencia y Privacidad (EULA)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "ACUERDO DE LICENCIA DE USUARIO FINAL (EULA)\n\n" +
                            "1. Aceptación de Términos: Al utilizar MiRuta, aceptas la recopilación y uso de tu ubicación para mostrar paradas y rutas cercanas.\n\n" +
                            "2. Protección de Datos: Las contraseñas se almacenan mediante algoritmos de encriptación segura (BCrypt) en una base de datos local SQLite (Room).\n\n" +
                            "3. Servicios de Ubicación: Los datos de geolocalización se procesan en tiempo real únicamente para la navegación y no son compartidos con terceros.\n\n" +
                            "4. Licencia de Uso: Se te otorga una licencia personal, no exclusiva e intransferible para usar la aplicación en dispositivos Android compatibles.\n\n" +
                            "Última actualización: Agosto 2026",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.semantics {
                    role = Role.Button
                    contentDescription = "Aceptar términos y cerrar"
                }
            ) {
                Text("Aceptar y Cerrar")
            }
        }
    )
}

@Composable
fun DialogoAyudaYSoporte(
    efectosActivados: Boolean,
    onToggleEfectos: (Boolean) -> Unit,
    talkbackActivado: Boolean,
    onToggleTalkback: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ayuda, Soporte y Accesibilidad",
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
                Text(
                    text = "Configuración de Pantalla y Accesibilidad",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Switch 1: Efectos de pantalla
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Efectos de pantalla", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Activar o desactivar animaciones y efectos visuales", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = efectosActivados,
                            onCheckedChange = onToggleEfectos,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFEB30F))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Switch 2: Lector de pantalla (TalkBack)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Asistente TalkBack", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Lectura de voz y etiquetas de accesibilidad", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = talkbackActivado,
                            onCheckedChange = onToggleTalkback,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFEB30F))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Soporte Técnico",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Correo: soporte@miruta.app\n• Soporte para Talkback y alto contraste\n• Centro de ayuda activo 24/7",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.semantics {
                    role = Role.Button
                    contentDescription = "Cerrar ventana de ayuda"
                }
            ) {
                Text("Entendido")
            }
        }
    )
}

@Composable
fun DialogoAgregarRutaFrecuente(
    onDismiss: () -> Unit,
    onConfirmar: (RutaFrecuenteItem) -> Unit
) {
    val context = LocalContext.current
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

    // Filtrar sugerencias según lo que escribe el usuario
    val sugerenciasFiltradas = remember(ubicacion) {
        if (ubicacion.isBlank()) emptyList()
        else sugerenciasUbicacion.filter { it.contains(ubicacion, ignoreCase = true) && it != ubicacion }
    }

    val coloresDisponibles = listOf(
        Color(0xFF4A86F7), // Azul
        Color(0xFFF26E68), // Rojo
        Color(0xFF2ECC71), // Verde
        Color(0xFF9B59B6), // Morado
        Color(0xFFF39C12), // Naranja
        Color(0xFF1ABC9C)  // Turquesa
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

                // BOTÓN DE UBICACIÓN ACTUAL GPS REAL
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

                // LISTA DE AUTOCOMPLETADO
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

                // CHIPS DE UBICACIONES RÁPIDAS
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
                                .clickable { iconoSeleccionado = iconoPair },
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
                                .clickable { colorSeleccionado = color }
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