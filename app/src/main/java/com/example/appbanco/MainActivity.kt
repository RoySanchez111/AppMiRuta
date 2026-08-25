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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appbanco.ui.components.TimeBasedBackground
import java.util.Calendar
import java.text.NumberFormat
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.compose.currentBackStackEntryAsState


import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.example.appbanco.ui.components.obtenerColoresFondo

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
                    color = Color(0xFF121212) // Fondo oscuro base para todas las pantallas
                ) {
                    NavegacionMiRuta()
                }
            }
        }
    }
}

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
//Clases nuevas
open class Usuario(
    val usuario: String,
    val password: String,
    val rol: String
)
class Persona(
    usuario: String,
    password: String,
    rol: String
): Usuario(usuario, password, rol)
class Conductor(
    usuario: String,
    password: String,
    rol: String
): Usuario(usuario, password, rol)

val roy = Persona("roy", "123", "Conductor")
val alex = Conductor("alex", "456", "Conductor")

val usuarios = listOf(
    roy,
    alex
)
@Composable
fun NavegacionMiRuta() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    // Mostrar barra y encabezado solo en pantallas de la app (no splash ni login)
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
            composable("guardados") { PantallaGuardados(navController) }
            composable("horario") { PantallaGenerica("Horario", Icons.Default.Schedule) }
            composable("alertas") { PantallaGenerica("Alertas", Icons.Default.Notifications) }
            composable("cuenta") { PantallaGenerica("Cuenta", Icons.Default.Person) }
        }
    }
}

@Composable
fun EncabezadoUsuario() {
    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val saludo = when (hora) {
        in 6..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }

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
                    text = "$saludo,",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "Roy Sanchez", // Nombre estático por ahora
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Círculo para foto de perfil
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { /* Acción de perfil */ },
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
        contentColor = Color(0xFF282869) // Azul oscuro de los degradados
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
                    selectedIconColor = Color(0xFFF4556D), // Pink/Red Ocaso
                    selectedTextColor = Color(0xFFF4556D),
                    unselectedIconColor = Color(0xFF282869),
                    unselectedTextColor = Color(0xFF282869),
                    indicatorColor = Color(0xFFFEB30F).copy(alpha = 0.2f) // Orange Amanecer tenue
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

fun obtenerLogoSegunHora(): Int {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 6..11 -> R.drawable.logo_miruta_amanecer
        in 12..19 -> R.drawable.logo_miruta_atardecer
        else -> R.drawable.logo_miruta_anochecer
    }
}

@Composable
fun PantallaSplash(navController: NavController){
    LaunchedEffect(Unit) {
        delay(2000.milliseconds)
        navController.navigate("login"){
            popUpTo("splash"){
                inclusive = true
            }
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
    var mensajeError by remember {mutableStateOf("")} //Variable de mensaje de error
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
        Text(
            "Mi Ruta", 
            fontSize = 36.sp, 
            fontWeight = FontWeight.Bold, 
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))

        //Muestra el mensaje de error
        if (mensajeError.isNotEmpty()){
            Text(
                text = mensajeError,
                color = Color.White
            )
        }
        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = {
                Text(
                    "Usuario", color = Color.Black,
                    modifier = Modifier.background(Color.White))//Fondo Blanco para la etiqueta
                },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                //Fondo del recuadro
                unfocusedContainerColor =Color.White,
                focusedContainerColor = Color.White,

                //Color del texto
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,

                //Color del borde
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,

                //Color del cursor
                cursorColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(
                "Contraseña", color = Color.Black,//Color del texto Contraseña
                modifier = Modifier.background(Color.White))//Fondo Blanco para la etiqueta
                    },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor =Color.White,

                //Color del texto
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,

                //Color del borde
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,

                //Color del cursor
                cursorColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (usuarios.any{
                    it.usuario == usuario && it.password == password
                    }) {
                    navController.navigate("principal") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                } else {
                    mensajeError = "Usuario o contraseña incorrecto"
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFEB30F), // Color Naranja Amanecer
                contentColor = Color.White
            )
        ) {
            Text("Iniciar Sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        //Botón de iniciar sesión con Google
        Button(
            onClick = {

            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black)
        )
        {
            Text("Iniciar sesión con Google")
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "O",
            color = Color.White,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        //Boton de iniciar sesión como invitado
        Button(
            onClick = {
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray,
                contentColor = Color.White
            )
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
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¿Listo para tu siguiente viaje?", fontSize = 16.sp, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("guardados") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF4556D), // Color Rosado/Rojo Ocaso
                        contentColor = Color.White
                    )
                ) {
                    Text("Explorar Mis Rutas", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f))
        ) {
            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun PantallaGuardados(
    navController: NavController
) {
    val lugaresGuardados =
        obtenerDatosGuardados { lugares ->
            lugares
        }

    val rutasCercanas = listOf(
        "Avenida 25 Poniente",
        "Plaza Angelópolis",
        "Universidad Tecmilenio",
        "Clínica Dénica"
    )

    var mostrarGuardados by rememberSaveable {
        mutableStateOf(false)
    }

    var mostrarRutas by rememberSaveable {
        mutableStateOf(false)
    }

    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "¿A dónde vamos hoy?",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 28.dp)
        )

            Button(
                onClick = {
                    mostrarGuardados = !mostrarGuardados
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2697B5).copy(alpha = 0.8f) // Azul Atardecer con transparencia
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Guardados"
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Guardados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector =
                        if (mostrarGuardados)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                    contentDescription = "Mostrar guardados"
                )
            }

            if (mostrarGuardados) {
                Spacer(modifier = Modifier.height(8.dp))

                lugaresGuardados.forEach { lugar ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar(
                                        "Seleccionaste: ${lugar.nombre}"
                                    )
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = lugar.icono,
                                contentDescription = lugar.nombre,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = lugar.nombre,
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Seleccionar",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    mostrarRutas = !mostrarRutas
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2697B5).copy(alpha = 0.8f) // Azul Atardecer con transparencia
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Rutas cercanas"
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Rutas cercanas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector =
                        if (mostrarRutas)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                    contentDescription = "Mostrar rutas"
                )
            }

            if (mostrarRutas) {
                Spacer(modifier = Modifier.height(8.dp))

                rutasCercanas.forEach { ruta ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar(
                                        "Ruta seleccionada: $ruta"
                                    )
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Ruta",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = ruta,
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Seleccionar ruta",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2697B5), // Color Azul Atardecer
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

            Spacer(modifier = Modifier.height(24.dp))
        }
}
