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
import androidx.navigation.NavController
import com.example.appbanco.data.alertasSimuladas
import com.example.appbanco.data.lineasDeRuta
import com.example.appbanco.ui.components.*
import com.example.appbanco.ui.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
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

@Composable
fun PantallaPrincipal(navController: NavController) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Coordenadas de Tecmilenio Campus Puebla
    val tecmilenioPuebla = LatLng(18.999446, -98.261833)

    // Inicializar MapLibre (solo una vez)
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
        // BUSCADOR
        Surface(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(18.dp),
            color = onBackground.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, onBackground.copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = onBackground.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Buscar línea o parada...", color = onBackground.copy(alpha = 0.4f), fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // MAPA MAPLIBRE CON OPENFREEMAP (Sin bloqueos, sin tarjetas)
        Box(modifier = Modifier.fillMaxWidth().height(400.dp).clip(RoundedCornerShape(20.dp))) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        getMapAsync { map ->
                            // Estilo de OpenFreeMap (Libre y nítido)
                            map.setStyle(Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty"))
                            
                            // Configuración inicial de cámara
                            map.cameraPosition = CameraPosition.Builder()
                                .target(tecmilenioPuebla)
                                .zoom(15.0)
                                .build()
                            
                            // Activar ubicación si los permisos están concedidos
                            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                map.locationComponent.apply {
                                    activateLocationComponent(
                                        LocationComponentActivationOptions.builder(ctx, map.style!!).build()
                                    )
                                    isLocationComponentEnabled = true
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    // El update se dispara cuando cambia el estado de Compose
                }
            )

            // Manejo de ciclos de vida de MapLibre
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    // Aquí se podrían propagar los eventos onStart, onResume, etc. al MapView si fuera necesario
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            IconButton(
                onClick = { /* Acción para centrar */ },
                modifier = Modifier.align(Alignment.BottomEnd).padding(14.dp).background(Color.White, CircleShape).shadow(4.dp, CircleShape)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación", tint = Color(0xFF4285F4))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHorarios(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val onBackground = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Buscar línea o parada...", color = onBackground.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = onBackground.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().height(52.dp).background(onBackground.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = onBackground.copy(alpha = 0.1f),
                focusedBorderColor = onBackground.copy(alpha = 0.3f),
                cursorColor = onBackground,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedTextColor = onBackground,
                focusedTextColor = onBackground
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = Color(0xFFFF3B30), shape = RoundedCornerShape(20.dp)) {
                Text("Ahora - 09:24", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
            }
            listOf("Mañana", "Lun 25 ago").forEach { texto ->
                Surface(
                    color = onBackground.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, onBackground.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(texto, color = onBackground.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.StarBorder, null, tint = onBackground.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("TODAS LAS LÍNEAS", color = onBackground.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))
        lineasDeRuta.forEach { LineCard(it, scope, snackbarHostState) }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Próximas salidas Plaza Mayor", color = onBackground.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = Color(0xFF389338), shape = RoundedCornerShape(20.dp)) {
                Text("09:25 - ahora", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
            }
            listOf("09:35", "09:45").forEach { texto ->
                Surface(
                    color = onBackground.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, onBackground.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(texto, color = onBackground.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2697B5), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver", fontWeight = FontWeight.Bold)
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
                .clickable { mostrarDialogo = true },
            shape = RoundedCornerShape(28.dp),
            color = surfaceColor,
            border = BorderStroke(1.dp, onSurface.copy(alpha = 0.1f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Box(modifier = Modifier.size(32.dp).background(primaryColor, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PriorityHigh, null, tint = Color.White, modifier = Modifier.size(18.dp))
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
            ),
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
fun PantallaCuenta(sessionManager: SessionManager, navController: NavController) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onBackground = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    var modoOffline by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(surfaceColor)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).background(onSurface.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Text("L", fontWeight = FontWeight.Bold, color = onSurface, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Leo Sanchez", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = onSurface)
                Text("leo.sanchez.lo@gmail.com", fontSize = 13.sp, color = onSurface.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Text("Rutas frecuentes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF4A86F7)) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Home, "Casa", tint = Color.White, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Casa", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        Text("Registrar ubicación", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                }
                Surface(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFFF26E68)) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.School, "Universidad", tint = Color.White, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Universidad", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        Text("Registrar ubicación", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Text("Modo Offline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = onSurface.copy(alpha = 0.05f)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Habilitar descarga de mapas", fontSize = 13.sp, color = onSurface.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
                    Switch(checked = modoOffline, onCheckedChange = { modoOffline = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFEB30F)))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), shape = RoundedCornerShape(24.dp), color = onBackground.copy(alpha = 0.03f)) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OpcionCuenta("Configuración"); OpcionCuenta("Alertas"); OpcionCuenta("Privacidad y Seguridad"); OpcionCuenta("Ayuda y Soporte")
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
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}