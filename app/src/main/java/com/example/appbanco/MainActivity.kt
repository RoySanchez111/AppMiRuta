package com.example.appbanco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appbanco.ui.components.TimeBasedBackground
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.clip
import androidx.navigation.compose.currentBackStackEntryAsState

// CONDICION 4: Interfaz simple
interface Autenticable {
    fun validar(usuario: String, pass: String): Boolean
}

// CONDICION 4: Objeto (Singleton) para configuración
object AppConfig {
    const val NOMBRE_APP = "Mi Ruta"
    // CONDICION 2: Colección para almacenar datos de usuario (Mapa)
    val usuariosPermitidos = mutableMapOf(
        "roy" to "123",
        "admin" to "admin"
    )
}

// CONDICION 4: Clase que implementa una interfaz
class ServicioAutenticacion : Autenticable {
    override fun validar(usuario: String, pass: String): Boolean {
        return AppConfig.usuariosPermitidos[usuario] == pass
    }
}

// CONDICION 1: Función con parámetros y retorno
fun obtenerMensajeBienvenida(usuario: String?): String {
    // CONDICION 3: Uso de null seguro (operador Elvis)
    val nombre = usuario ?: "Invitado"
    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val saludo = when (hora) {
        in 6..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
    return "$saludo, $nombre"
}

fun formatearRuta(nombre: String): String {
    return "Ruta: ${nombre.uppercase()}"
}

fun obtenerLogoSegunHora(): Int {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 6..11 -> R.drawable.logo_miruta_amanecer
        in 12..18 -> R.drawable.logo_miruta_atardecer
        else -> R.drawable.logo_miruta_anochecer
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashTheme = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> R.style.Theme_MiRuta_Splash_Madrugada
            in 6..11 -> R.style.Theme_MiRuta_Splash_Manana
            in 12..17 -> R.style.Theme_MiRuta_Splash_Atardecer
            in 18..19 -> R.style.Theme_MiRuta_Splash_Ocaso
            else -> R.style.Theme_MiRuta_Splash_Noche
        }
        setTheme(splashTheme)

        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212)
                ) {
                    NavegacionMiRuta()
                }
            }
        }
    }
}

data class LineaRuta(
    val numero: String,
    val color: Color,
    val nombre: String,
    val destino: String,
    val frecuencia: String,
    val iconoEstado: ImageVector = Icons.Default.Star
)

val lineasDeRuta = listOf(
    LineaRuta("L1", Color(0xFFFF8E56), "Línea 1", "Plaza Mayor → Aeropuerto", "Cada 8 min"),
    LineaRuta("L4", Color(0xFF327CF2), "Línea 4", "Universidad → Puerto", "Cada 12 min"),
    LineaRuta("L7", Color(0xFF0DBC61), "Línea 7", "Centro → Hospital Norte", "Cada 10 min"),
    LineaRuta("MA", Color(0xFFA149A1), "Metro A", "Central → Torre Central", "Cada 4 min"),
    LineaRuta("L5", Color(0xFFFF8E56), "Línea 5", "Mercado → Estadio", "Cada 15 min")
)

data class Lugar(val nombre: String, val icono: ImageVector)

fun obtenerDatosGuardados(callback: (List<Lugar>) -> List<Lugar>): List<Lugar> {
    val lista = listOf(
        Lugar("Casa", Icons.Default.Home),
        Lugar("Trabajo", Icons.Default.Work),
        Lugar("Escuela", Icons.Default.School),
        Lugar("Gimnasio", Icons.Default.FitnessCenter)
    )
    return callback(lista)
}

open class Usuario(
    val usuario: String,
    val password: String,
    val rol: String
)

class Persona(usuario: String, password: String, rol: String): Usuario(usuario, password, rol)
class Conductor(usuario: String, password: String, rol: String): Usuario(usuario, password, rol)

val roy = Persona("roy", "123", "Conductor")
val alex = Conductor("alex", "456", "Conductor")
val usuarios = listOf(roy, alex)

@Composable
fun NavegacionMiRuta() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val esPantallaApp = rutaActual != "splash" && rutaActual != "login"

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            if (esPantallaApp) {
                EncabezadoUsuario()
            }
        },
        bottomBar = {
            if (esPantallaApp) {
                BarraNavegacionInferior(navController, rutaActual)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController, 
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("splash") {
                TimeBasedBackground { PantallaSplash(navController) }
            }
            composable("login") {
                TimeBasedBackground { PantallaLogin(navController) }
            }
            composable("principal") { PantallaPrincipal(navController) }
            composable("horario") { PantallaHorarios(navController) }
            composable("alertas") { PantallaGenerica("Alertas", Icons.Default.Notifications) }
            composable("cuenta") { PantallaGenerica("Cuenta", Icons.Default.Person) }
        }
    }
}

@Composable
fun EncabezadoUsuario() {
    val saludo = obtenerMensajeBienvenida("Roy Sanchez")

    TimeBasedBackground(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = saludo.split(",")[0] + ",",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = saludo.split(",")[1].trim(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun BarraNavegacionInferior(navController: NavController, rutaActual: String?) {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.85f),
        contentColor = Color(0xFF282869)
    ) {
        val items = listOf(
            Triple("principal", "Inicio", Icons.Default.Home),
            Triple("horario", "Horario", Icons.Default.Schedule),
            Triple("alertas", "Alertas", Icons.Default.Notifications),
            Triple("cuenta", "Cuenta", Icons.Default.Person)
        )

        items.forEach { (ruta, etiqueta, icono) ->
            NavigationBarItem(
                icon = { Icon(icono, contentDescription = etiqueta) },
                label = { Text(etiqueta) },
                selected = rutaActual == ruta,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFF4556D),
                    selectedTextColor = Color(0xFFF4556D),
                    unselectedIconColor = Color(0xFF282869),
                    unselectedTextColor = Color(0xFF282869),
                    indicatorColor = Color(0xFF00BCD4).copy(alpha = 0.2f)
                ),
                onClick = {
                    if (rutaActual != ruta) {
                        navController.navigate(ruta) {
                            popUpTo("principal") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun PantallaGenerica(titulo: String, icono: ImageVector) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text(titulo, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PantallaSplash(navController: NavController){
    LaunchedEffect(Unit) {
        delay(2000.milliseconds)
        navController.navigate("login"){
            popUpTo("splash"){ inclusive = true }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = painterResource(id = obtenerLogoSegunHora()),
            contentDescription = "Logo MiRuta",
            modifier = Modifier.size(240.dp)
        )
    }
}

@Composable
fun PantallaLogin(navController: NavController) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var iniciarAnimacion by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (iniciarAnimacion) 1f else 0f,
        animationSpec = tween(durationMillis = 1500), label = "alphaAnim"
    )

    LaunchedEffect(Unit) { iniciarAnimacion = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Mi Ruta", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))

        if (mensajeError.isNotEmpty()){
            Text(text = mensajeError, color = Color.White)
        }
        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = {
                Text("Usuario", color = Color.Black, modifier = Modifier.background(Color.White))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                cursorColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text("Contraseña", color = Color.Black, modifier = Modifier.background(Color.White))
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                cursorColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (usuarios.any { it.usuario == usuario && it.password == password }) {
                    navController.navigate("principal") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    mensajeError = "Usuario o contraseña incorrecto"
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFEB30F),
                contentColor = Color.White
            )
        ) {
            Text("Iniciar Sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text("Iniciar sesión con Google")
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "O", color = Color.White, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White)
        ) {
            Text("Continuar como invitado")
        }
    }
}


@Composable
fun PantallaPrincipal(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // =================================================
        // BUSCADOR
        // =================================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color(0xFFFF6B4A)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Buscar línea o parada...",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =================================================
        // MAPA SIMULADO
        // =================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Hace que el mapa ocupe el espacio disponible
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .background(
                    color = Color(0xFFE7E3DB),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Calles horizontales
                drawLine(
                    color = Color(0xFFCFCBC4),
                    start = Offset(0f, size.height * 0.30f),
                    end = Offset(size.width, size.height * 0.30f),
                    strokeWidth = 28f
                )
                drawLine(
                    color = Color(0xFFCFCBC4),
                    start = Offset(0f, size.height * 0.60f),
                    end = Offset(size.width, size.height * 0.60f),
                    strokeWidth = 32f
                )

                // Calles verticales
                drawLine(
                    color = Color(0xFFCFCBC4),
                    start = Offset(size.width * 0.25f, 0f),
                    end = Offset(size.width * 0.25f, size.height),
                    strokeWidth = 28f
                )
                drawLine(
                    color = Color(0xFFCFCBC4),
                    start = Offset(size.width * 0.70f, 0f),
                    end = Offset(size.width * 0.70f, size.height),
                    strokeWidth = 28f
                )

                // Ruta azul
                drawLine(
                    color = Color(0xFF4DA3E8),
                    start = Offset(size.width * 0.15f, size.height * 0.67f),
                    end = Offset(size.width * 0.85f, size.height * 0.67f),
                    strokeWidth = 12f,
                    cap = StrokeCap.Round
                )

                // Ruta naranja
                drawLine(
                    color = Color(0xFFFFA23A),
                    start = Offset(size.width * 0.20f, size.height * 0.40f),
                    end = Offset(size.width * 0.75f, size.height * 0.40f),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )

                // Paradas
                val paradas = listOf(
                    Offset(size.width * 0.25f, size.height * 0.67f),
                    Offset(size.width * 0.45f, size.height * 0.67f),
                    Offset(size.width * 0.65f, size.height * 0.67f),
                    Offset(size.width * 0.80f, size.height * 0.67f)
                )
                paradas.forEach {
                    drawCircle(color = Color.White, radius = 13f, center = it)
                    drawCircle(color = Color(0xFFE53935), radius = 7f, center = it)
                }

                // Ubicación usuario
                val ubicacion = Offset(size.width * 0.52f, size.height * 0.78f)
                drawCircle(color = Color.White, radius = 18f, center = ubicacion)
                drawCircle(color = Color(0xFF4285F4), radius = 11f, center = ubicacion)
            }

            // Botón de ubicación
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .size(45.dp),
                shape = RoundedCornerShape(50),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                IconButton(onClick = { /* GPS Logic */ }) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = Color(0xFF4285F4)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHorarios(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = CoroutineScope(Dispatchers.Main)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Buscar línea o parada...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                cursorColor = Color.White,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Color(0xFFFF3B30).copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = "Ahora - 09:24",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            listOf("Mañana", "Lun 25 ago", "Mar 26 ago").forEach { texto ->
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = texto,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "TODAS LAS LÍNEAS",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        lineasDeRuta.forEach { linea ->
            LineCard(linea = linea, scope = scope, snackbarHostState = snackbarHostState)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Próximas salidas desde Plaza Mayor",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Color(0xFF389338).copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = "09:25 - ahora",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            listOf("09:35", "09:45", "09:55", "10:05").forEach { hora ->
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = hora,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2697B5),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LineCard(linea: LineaRuta, scope: CoroutineScope, snackbarHostState: SnackbarHostState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable {
                scope.launch {
                    snackbarHostState.showSnackbar("Seleccionaste: ${linea.nombre}")
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
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
                    text = linea.numero,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = linea.nombre,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = linea.iconoEstado,
                        contentDescription = null,
                        tint = linea.color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = linea.destino,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = linea.frecuencia,
                    color = linea.color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Seleccionar",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}