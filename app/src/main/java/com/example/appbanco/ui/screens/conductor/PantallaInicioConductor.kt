package com.example.appbanco.ui.screens.conductor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appbanco.data.database.SyncManager
import com.example.appbanco.logic.SessionManager
import com.example.appbanco.logic.ejecutarVibracionHaptica
import com.example.appbanco.ui.screens.conductor.components.*
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PantallaInicioConductor(
    navController: NavController
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val syncManager = remember { SyncManager() }

    val currentUsernameState = sessionManager.currentUsername.collectAsState(initial = "rafa")
    val nombreConductor = currentUsernameState.value ?: "rafa"
    val ubicacionTexto = "Puebla - Ruta Troncal Activa"

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    var ubicacionActual by remember {
        mutableStateOf(Point.fromLngLat(-98.261833, 18.999446))
    }

    var enServicio by remember { mutableStateOf(true) }
    val fusedLocationClient = remember(context) { LocationServices.getFusedLocationProviderClient(context.applicationContext) }

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
        } catch (_: Exception) {
            onSuccess(defaultPoint)
        }
    }

    fun obtenerGpsReal(onSuccess: (Point) -> Unit) {
        val defaultPoint = Point.fromLngLat(-98.261833, 18.999446)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        val pt = Point.fromLngLat(loc.longitude, loc.latitude)
                        onSuccess(pt)
                    } else {
                        fallbackToNativeLocation(context, onSuccess, defaultPoint)
                    }
                }.addOnFailureListener {
                    fallbackToNativeLocation(context, onSuccess, defaultPoint)
                }
            } catch (e: Exception) {
                fallbackToNativeLocation(context, onSuccess, defaultPoint)
            }
        } else {
            onSuccess(defaultPoint)
        }
    }

    // Transmitir ubicación GPS en tiempo real a Firebase cuando el conductor está en servicio
    LaunchedEffect(enServicio) {
        if (enServicio) {
            android.util.Log.d("LOCATION_SYNC", "Driver activa ubicación: $nombreConductor")
            while (isActive && enServicio) {
                obtenerGpsReal { realPoint ->
                    ubicacionActual = realPoint
                    scope.launch {
                        syncManager.broadcastConductorLocation(
                            conductorId = nombreConductor,
                            nombre = nombreConductor,
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
            android.util.Log.d("LOCATION_SYNC", "Driver desactiva ubicación: $nombreConductor")
            // Notificar a Firestore que la transmisión del conductor se pausó
            scope.launch {
                syncManager.broadcastConductorLocation(
                    conductorId = nombreConductor,
                    nombre = nombreConductor,
                    ruta = "Línea L1",
                    lat = ubicacionActual.latitude(),
                    lng = ubicacionActual.longitude(),
                    activo = false
                )
                android.util.Log.d("LOCATION_SYNC", "Backend actualizado locationEnabled=false driver=$nombreConductor")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        EncabezadoConductor(
            nombre = nombreConductor,
            ubicacion = ubicacionTexto,
            enServicio = enServicio,
            onAvatarClick = { navController.navigate("cuenta_conductor") }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            MapaConductor(
                ubicacionActual = ubicacionActual,
                enServicio = enServicio,
                onActualizar = {
                    ejecutarVibracionHaptica(context, 40L)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    obtenerGpsReal { pt ->
                        ubicacionActual = pt
                        Toast.makeText(context, "🔄 Ubicación GPS actualizada", Toast.LENGTH_SHORT).show()
                    }
                },
                onCentrarUbicacion = {
                    ejecutarVibracionHaptica(context, 50L)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    obtenerGpsReal { pt ->
                        ubicacionActual = pt
                        Toast.makeText(context, "🎯 Mapa centrado en tu posición", Toast.LENGTH_SHORT).show()
                    }
                },
                onAlerta = {
                    ejecutarVibracionHaptica(context, 60L)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate("alertas_conductor")
                }
            )
        }

        EstadoServicio(
            enServicio = enServicio,
            onEstadoChange = { nuevoEstado ->
                ejecutarVibracionHaptica(context, 45L)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                enServicio = nuevoEstado
                Toast.makeText(
                    context,
                    if (nuevoEstado) "🟢 En servicio - Transmitiendo ubicación" else "🔴 Fuera de servicio - Transmisión pausada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        BarraNavegacionConductor(
            navController = navController,
            rutaActual = rutaActual
        )
    }
}
