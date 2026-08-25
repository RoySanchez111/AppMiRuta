package com.example.appbanco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashTheme =
            when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {

                in 0..5 ->
                    R.style.Theme_MiRuta_Splash_Madrugada

                in 6..11 ->
                    R.style.Theme_MiRuta_Splash_Manana

                in 12..17 ->
                    R.style.Theme_MiRuta_Splash_Atardecer

                in 18..19 ->
                    R.style.Theme_MiRuta_Splash_Ocaso

                else ->
                    R.style.Theme_MiRuta_Splash_Noche
            }

        setTheme(splashTheme)

        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {

            MaterialTheme {

                TimeBasedBackground {

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {

                        NavegacionMiRuta()
                    }
                }
            }
        }
    }
}




data class Lugar(
    val nombre: String,
    val icono: ImageVector
)


fun obtenerDatosGuardados(
    callback: (List<Lugar>) -> List<Lugar>
): List<Lugar> {

    val lista =
        listOf(
            Lugar(
                "Casa",
                Icons.Default.Home
            ),
            Lugar(
                "Trabajo",
                Icons.Default.Work
            ),
            Lugar(
                "Escuela",
                Icons.Default.School
            ),
            Lugar(
                "Gimnasio",
                Icons.Default.FitnessCenter
            )
        )

    return callback(lista)
}


// usuarios

open class Usuario(
    val usuario: String,
    val password: String,
    val rol: String
)


class Persona(
    usuario: String,
    password: String,
    rol: String
) : Usuario(
    usuario,
    password,
    rol
)


class Conductor(
    usuario: String,
    password: String,
    rol: String
) : Usuario(
    usuario,
    password,
    rol
)


val roy =
    Persona(
        "roy",
        "123",
        "Conductor"
    )


val alex =
    Conductor(
        "alex",
        "456",
        "Conductor"
    )


val usuarios =
    listOf(
        roy,
        alex
    )


// NAVEGACIÓN
@Composable
fun NavegacionMiRuta() {

    val navController =
        rememberNavController()


    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {

            PantallaSplash(
                navController
            )
        }


        composable("login") {

            PantallaLogin(
                navController
            )
        }


        composable("principal") {

            PantallaPrincipal(
                navController
            )
        }


        composable("guardados") {

            PantallaGuardados(
                navController
            )
        }
    }
}

// SPLASH


@Composable
fun PantallaSplash(
    navController: NavController
) {

    LaunchedEffect(Unit) {

        delay(
            2000.milliseconds
        )


        navController.navigate(
            "login"
        ) {

            popUpTo(
                "splash"
            ) {

                inclusive = true
            }
        }
    }


    Box(
        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center
    ) {

        Image(
            painter =
                painterResource(
                    id = R.drawable.logo_miruta
                ),

            contentDescription =
                "Logo MiRuta",

            modifier =
                Modifier.size(
                    240.dp
                )
        )
    }
}

// LOGIN

@Composable
fun PantallaLogin(
    navController: NavController
) {

    var usuario by
    remember {
        mutableStateOf("")
    }


    var password by
    remember {
        mutableStateOf("")
    }


    var mensajeError by
    remember {
        mutableStateOf("")
    }


    var iniciarAnimacion by
    remember {
        mutableStateOf(false)
    }


    val alpha by
    animateFloatAsState(

        targetValue =
            if (iniciarAnimacion)
                1f
            else
                0f,

        animationSpec =
            tween(
                durationMillis = 1500
            ),

        label = "alphaAnim"
    )


    LaunchedEffect(Unit) {

        iniciarAnimacion = true
    }


    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(
                    rememberScrollState()
                )
                .alpha(alpha),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "Mi Ruta",

            fontSize = 36.sp,

            fontWeight =
                FontWeight.Bold,

            color = Color.White
        )


        Spacer(
            modifier =
                Modifier.height(
                    32.dp
                )
        )


        if (
            mensajeError.isNotEmpty()
        ) {

            Text(
                text = mensajeError,

                color = Color.White
            )


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )
        }


        OutlinedTextField(

            value = usuario,

            onValueChange = {
                usuario = it
            },

            label = {

                Text(
                    text = "Usuario",

                    color =
                        Color.Black,

                    modifier =
                        Modifier.background(
                            Color.White
                        )
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            colors =
                OutlinedTextFieldDefaults.colors(

                    unfocusedContainerColor =
                        Color.White,

                    focusedContainerColor =
                        Color.White,

                    unfocusedTextColor =
                        Color.Black,

                    focusedTextColor =
                        Color.Black,

                    unfocusedBorderColor =
                        Color.Transparent,

                    focusedBorderColor =
                        Color.Transparent,

                    cursorColor =
                        Color.Black
                )
        )


        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )


        OutlinedTextField(

            value = password,

            onValueChange = {
                password = it
            },

            label = {

                Text(
                    text =
                        "Contraseña",

                    color =
                        Color.Black,

                    modifier =
                        Modifier.background(
                            Color.White
                        )
                )
            },

            visualTransformation =
                PasswordVisualTransformation(),

            modifier =
                Modifier.fillMaxWidth(),

            colors =
                OutlinedTextFieldDefaults.colors(

                    unfocusedContainerColor =
                        Color.White,

                    focusedContainerColor =
                        Color.White,

                    unfocusedTextColor =
                        Color.Black,

                    focusedTextColor =
                        Color.Black,

                    unfocusedBorderColor =
                        Color.Transparent,

                    focusedBorderColor =
                        Color.Transparent,

                    cursorColor =
                        Color.Black
                )
        )


        Spacer(
            modifier =
                Modifier.height(
                    32.dp
                )
        )


        Button(

            onClick = {

                if (
                    usuarios.any {

                        it.usuario ==
                                usuario &&
                                it.password ==
                                password
                    }
                ) {

                    navController.navigate(
                        "principal"
                    ) {

                        popUpTo(
                            "login"
                        ) {

                            inclusive = true
                        }
                    }

                } else {

                    mensajeError =
                        "Usuario o contraseña incorrecto"
                }
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
        ) {

            Text(
                text =
                    "Iniciar Sesión",

                fontSize =
                    18.sp
            )
        }


        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )


        Button(

            onClick = {

            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),

            colors =
                ButtonDefaults.buttonColors(

                    containerColor =
                        Color.White,

                    contentColor =
                        Color.Black
                )
        ) {

            Text(
                text =
                    "Iniciar sesión con Google"
            )
        }


        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )


        Text(
            text = "O",

            color = Color.White,

            fontSize = 18.sp
        )


        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )


        Button(

            onClick = {

            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),

            colors =
                ButtonDefaults.buttonColors(

                    containerColor =
                        Color.Gray,

                    contentColor =
                        Color.White
                )
        ) {

            Text(
                text =
                    "Continuar como invitado"
            )
        }
    }
}

// pantalla principal

@Composable
fun PantallaPrincipal(
    navController: NavController
) {

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = 70.dp
                    )
                    .verticalScroll(
                        rememberScrollState()
                    )
        ) {

            // =================================================
            // encabezado

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 24.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            "Buenos días,",

                        fontSize =
                            16.sp,

                        color =
                            Color.White.copy(
                                alpha = 0.8f
                            )
                    )


                    Text(
                        text =
                            "Usuario",

                        fontSize =
                            26.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            Color.White
                    )


                    Text(
                        text =
                            "¿A dónde vamos?",

                        fontSize =
                            15.sp,

                        color =
                            Color.White.copy(
                                alpha = 0.75f
                            )
                    )
                }


                // Avatar
                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .background(
                                color =
                                    Color(
                                        0xFFFF6B4A
                                    ),

                                shape =
                                    RoundedCornerShape(
                                        50
                                    )
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "U",

                        color =
                            Color.White,

                        fontSize =
                            20.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        20.dp
                    )
            )


            // =================================================
            // BUSCADOR
            // =================================================

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal =
                                20.dp
                        )
                        .height(
                            52.dp
                        ),

                shape =
                    RoundedCornerShape(
                        18.dp
                    ),

                color =
                    Color.White,

                shadowElevation =
                    4.dp
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal =
                                    16.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Search,

                        contentDescription =
                            "Buscar",

                        tint =
                            Color(
                                0xFFFF6B4A
                            )
                    )


                    Spacer(
                        modifier =
                            Modifier.width(
                                10.dp
                            )
                    )


                    Text(
                        text =
                            "Buscar línea o parada...",

                        color =
                            Color.Gray,

                        fontSize =
                            14.sp
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        20.dp
                    )
            )

            // mapa simulado

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            330.dp
                        )
                        .padding(
                            horizontal =
                                20.dp
                        )
                        .background(
                            color =
                                Color(
                                    0xFFE7E3DB
                                ),

                            shape =
                                RoundedCornerShape(
                                    20.dp
                                )
                        )
            ) {

                Canvas(
                    modifier =
                        Modifier.fillMaxSize()
                ) {

                    // Calles horizontales
                    drawLine(
                        color =
                            Color(
                                0xFFCFCBC4
                            ),

                        start =
                            Offset(
                                0f,
                                size.height *
                                        0.30f
                            ),

                        end =
                            Offset(
                                size.width,
                                size.height *
                                        0.30f
                            ),

                        strokeWidth =
                            28f
                    )


                    drawLine(
                        color =
                            Color(
                                0xFFCFCBC4
                            ),

                        start =
                            Offset(
                                0f,
                                size.height *
                                        0.60f
                            ),

                        end =
                            Offset(
                                size.width,
                                size.height *
                                        0.60f
                            ),

                        strokeWidth =
                            32f
                    )


                    // Calles verticales
                    drawLine(
                        color =
                            Color(
                                0xFFCFCBC4
                            ),

                        start =
                            Offset(
                                size.width *
                                        0.25f,
                                0f
                            ),

                        end =
                            Offset(
                                size.width *
                                        0.25f,
                                size.height
                            ),

                        strokeWidth =
                            28f
                    )


                    drawLine(
                        color =
                            Color(
                                0xFFCFCBC4
                            ),

                        start =
                            Offset(
                                size.width *
                                        0.70f,
                                0f
                            ),

                        end =
                            Offset(
                                size.width *
                                        0.70f,
                                size.height
                            ),

                        strokeWidth =
                            28f
                    )


                    // Ruta azul
                    drawLine(
                        color =
                            Color(
                                0xFF4DA3E8
                            ),

                        start =
                            Offset(
                                size.width *
                                        0.15f,

                                size.height *
                                        0.67f
                            ),

                        end =
                            Offset(
                                size.width *
                                        0.85f,

                                size.height *
                                        0.67f
                            ),

                        strokeWidth =
                            12f,

                        cap =
                            StrokeCap.Round
                    )


                    // Ruta naranja
                    drawLine(
                        color =
                            Color(
                                0xFFFFA23A
                            ),

                        start =
                            Offset(
                                size.width *
                                        0.20f,

                                size.height *
                                        0.40f
                            ),

                        end =
                            Offset(
                                size.width *
                                        0.75f,

                                size.height *
                                        0.40f
                            ),

                        strokeWidth =
                            10f,

                        cap =
                            StrokeCap.Round
                    )


                    // Paradas
                    val paradas =
                        listOf(

                            Offset(
                                size.width *
                                        0.25f,

                                size.height *
                                        0.67f
                            ),

                            Offset(
                                size.width *
                                        0.45f,

                                size.height *
                                        0.67f
                            ),

                            Offset(
                                size.width *
                                        0.65f,

                                size.height *
                                        0.67f
                            ),

                            Offset(
                                size.width *
                                        0.80f,

                                size.height *
                                        0.67f
                            )
                        )


                    paradas.forEach {

                        drawCircle(
                            color =
                                Color.White,

                            radius =
                                13f,

                            center =
                                it
                        )


                        drawCircle(
                            color =
                                Color(
                                    0xFFE53935
                                ),

                            radius =
                                7f,

                            center =
                                it
                        )
                    }


                    // Ubicación usuario
                    val ubicacion =
                        Offset(
                            size.width *
                                    0.52f,

                            size.height *
                                    0.78f
                        )


                    drawCircle(
                        color =
                            Color.White,

                        radius =
                            18f,

                        center =
                            ubicacion
                    )


                    drawCircle(
                        color =
                            Color(
                                0xFF4285F4
                            ),

                        radius =
                            11f,

                        center =
                            ubicacion
                    )
                }


                // Botón de ubicación
                Surface(
                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                            .padding(
                                14.dp
                            )
                            .size(
                                45.dp
                            ),

                    shape =
                        RoundedCornerShape(
                            50
                        ),

                    color =
                        Color.White,

                    shadowElevation =
                        4.dp
                ) {

                    IconButton(
                        onClick = {

                            // Después se conectará :) wii
                            // con GPS
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.MyLocation,

                            contentDescription =
                                "Mi ubicación",

                            tint =
                                Color(
                                    0xFF4285F4
                                )
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        22.dp
                    )
            )

            // rutas cercanas

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal =
                                20.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "Rutas cercanas",

                    fontSize =
                        20.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        Color.White,

                    modifier =
                        Modifier.weight(1f)
                )


                TextButton(
                    onClick = {

                        navController.navigate(
                            "guardados"
                        )
                    }
                ) {

                    Text(
                        text =
                            "Ver más",

                        color =
                            Color.White
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            // tarjeta ruta
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal =
                                20.dp
                        ),

                shape =
                    RoundedCornerShape(
                        16.dp
                    ),

                colors =
                    CardDefaults.cardColors(

                        containerColor =
                            Color.White
                    ),

                elevation =
                    CardDefaults.cardElevation(

                        defaultElevation =
                            3.dp
                    )
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                16.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier =
                            Modifier
                                .size(
                                    42.dp
                                )
                                .background(
                                    color =
                                        Color(
                                            0xFF4DA3E8
                                        ),

                                    shape =
                                        RoundedCornerShape(
                                            12.dp
                                        )
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.DirectionsBus,

                            contentDescription =
                                "Ruta",

                            tint =
                                Color.White
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.width(
                                14.dp
                            )
                    )


                    Column(
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    ) {

                        Text(
                            text =
                                "Ruta cercana",

                            fontWeight =
                                FontWeight.Bold,

                            fontSize =
                                16.sp,

                            color =
                                Color.Black
                        )


                        Text(
                            text =
                                "Parada más cercana",

                            fontSize =
                                13.sp,

                            color =
                                Color.Gray
                        )
                    }


                    Text(
                        text =
                            "En línea",

                        color =
                            Color(
                                0xFF43A047
                            ),

                        fontSize =
                            12.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        90.dp
                    )
            )
        }


        // botones simulados

        Row(
            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 12.dp
                    ),

            horizontalArrangement =
                Arrangement.SpaceAround,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick = {

                    // Después se reemplazará
                }
            ) {

                Text(
                    text = "Inicio",

                    color =
                        Color.White,

                    fontSize =
                        12.sp
                )
            }


            TextButton(
                onClick = {

                    // Después se reemplazará
                }
            ) {

                Text(
                    text =
                        "Horarios",

                    color =
                        Color.White,

                    fontSize =
                        12.sp
                )
            }


            TextButton(
                onClick = {

                    // Después se reemplazará
                }
            ) {

                Text(
                    text =
                        "Alertas",

                    color =
                        Color.White,

                    fontSize =
                        12.sp
                )
            }


            TextButton(
                onClick = {

                    // Después se reemplazará
                }
            ) {

                Text(
                    text =
                        "Cuenta",

                    color =
                        Color.White,

                    fontSize =
                        12.sp
                )
            }
        }
    }
}

// Pantalla guardados

@Composable
fun PantallaGuardados(
    navController: NavController
) {

    val lugaresGuardados =
        obtenerDatosGuardados {

                lugares ->
            lugares
        }


    val rutasCercanas =
        listOf(
            "Avenida 25 Poniente",
            "Plaza Angelópolis",
            "Universidad Tecmilenio",
            "Clínica Dénica"
        )


    var mostrarGuardados by
    rememberSaveable {

        mutableStateOf(false)
    }


    var mostrarRutas by
    rememberSaveable {

        mutableStateOf(false)
    }


    val snackbarHostState =
        remember {

            SnackbarHostState()
        }


    Scaffold(
        containerColor =
            Color.Transparent,

        snackbarHost = {

            SnackbarHost(
                snackbarHostState
            )
        }
    ) {

            paddingValues ->


        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        paddingValues
                    )
                    .padding(
                        24.dp
                    )
                    .verticalScroll(
                        rememberScrollState()
                    ),

            horizontalAlignment =
                Alignment.Start,

            verticalArrangement =
                Arrangement.Top
        ) {

            Text(
                text =
                    "Buenos días,",

                fontSize =
                    24.sp,

                color =
                    Color.White.copy(
                        alpha = 0.8f
                    ),

                modifier =
                    Modifier.padding(
                        top = 16.dp
                    )
            )


            Text(
                text =
                    "Usuario",

                fontSize =
                    32.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color.White
            )


            Text(
                text =
                    "¿A dónde vamos?",

                fontSize =
                    18.sp,

                color =
                    Color.White.copy(
                        alpha = 0.7f
                    ),

                modifier =
                    Modifier.padding(
                        bottom = 28.dp
                    )
            )

            // guardados

            Button(

                onClick = {

                    mostrarGuardados =
                        !mostrarGuardados
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            60.dp
                        ),

                colors =
                    ButtonDefaults.buttonColors(

                        containerColor =
                            Color.White.copy(
                                alpha = 0.18f
                            )
                    ),

                shape =
                    RoundedCornerShape(
                        12.dp
                    )
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Favorite,

                    contentDescription =
                        "Guardados"
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            12.dp
                        )
                )


                Text(
                    text =
                        "Guardados",

                    fontSize =
                        18.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )


                Icon(
                    imageVector =

                        if (
                            mostrarGuardados
                        )

                            Icons.Default.KeyboardArrowUp

                        else

                            Icons.Default.KeyboardArrowDown,

                    contentDescription =
                        "Mostrar guardados"
                )
            }


            if (
                mostrarGuardados
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )


                lugaresGuardados.forEach {

                        lugar ->


                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical =
                                        4.dp
                                )
                                .clickable {

                                    CoroutineScope(
                                        Dispatchers.Main
                                    ).launch {

                                        snackbarHostState.showSnackbar(

                                            "Seleccionaste: ${lugar.nombre}"
                                        )
                                    }
                                },

                        colors =
                            CardDefaults.cardColors(

                                containerColor =
                                    Color.White.copy(
                                        alpha =
                                            0.12f
                                    )
                            ),

                        shape =
                            RoundedCornerShape(
                                10.dp
                            )
                    ) {

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        16.dp
                                    ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector =
                                    lugar.icono,

                                contentDescription =
                                    lugar.nombre,

                                tint =
                                    Color.White,

                                modifier =
                                    Modifier.size(
                                        26.dp
                                    )
                            )


                            Spacer(
                                modifier =
                                    Modifier.width(
                                        14.dp
                                    )
                            )


                            Text(
                                text =
                                    lugar.nombre,

                                color =
                                    Color.White,

                                fontSize =
                                    16.sp,

                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )


                            Icon(
                                imageVector =
                                    Icons.Default.ChevronRight,

                                contentDescription =
                                    "Seleccionar",

                                tint =
                                    Color.White.copy(
                                        alpha =
                                            0.7f
                                    )
                            )
                        }
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )


            // Rutas cerquitas

            Button(

                onClick = {

                    mostrarRutas =
                        !mostrarRutas
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            60.dp
                        ),

                colors =
                    ButtonDefaults.buttonColors(

                        containerColor =
                            Color.White.copy(
                                alpha = 0.18f
                            )
                    ),

                shape =
                    RoundedCornerShape(
                        12.dp
                    )
            ) {

                Icon(
                    imageVector =
                        Icons.Default.LocationOn,

                    contentDescription =
                        "Rutas cercanas"
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            12.dp
                        )
                )


                Text(
                    text =
                        "Rutas cercanas",

                    fontSize =
                        18.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )


                Icon(
                    imageVector =

                        if (
                            mostrarRutas
                        )

                            Icons.Default.KeyboardArrowUp

                        else

                            Icons.Default.KeyboardArrowDown,

                    contentDescription =
                        "Mostrar rutas"
                )
            }


            if (
                mostrarRutas
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )


                rutasCercanas.forEach {

                        ruta ->


                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical =
                                        4.dp
                                )
                                .clickable {

                                    CoroutineScope(
                                        Dispatchers.Main
                                    ).launch {

                                        snackbarHostState.showSnackbar(

                                            "Ruta seleccionada: $ruta"
                                        )
                                    }
                                },

                        colors =
                            CardDefaults.cardColors(

                                containerColor =
                                    Color.White.copy(
                                        alpha =
                                            0.12f
                                    )
                            ),

                        shape =
                            RoundedCornerShape(
                                10.dp
                            )
                    ) {

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        16.dp
                                    ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.LocationOn,

                                contentDescription =
                                    "Ruta",

                                tint =
                                    Color.White,

                                modifier =
                                    Modifier.size(
                                        26.dp
                                    )
                            )


                            Spacer(
                                modifier =
                                    Modifier.width(
                                        14.dp
                                    )
                            )


                            Text(
                                text =
                                    ruta,

                                color =
                                    Color.White,

                                fontSize =
                                    16.sp,

                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )


                            Icon(
                                imageVector =
                                    Icons.Default.ChevronRight,

                                contentDescription =
                                    "Seleccionar ruta",

                                tint =
                                    Color.White.copy(
                                        alpha =
                                            0.7f
                                    )
                            )
                        }
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        28.dp
                    )
            )


            Button(

                onClick = {

                    navController.popBackStack()
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            50.dp
                        )
            ) {

                Icon(
                    imageVector =
                        Icons.AutoMirrored.Filled.ArrowBack,

                    contentDescription =
                        "Volver"
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )


                Text(
                    text =
                        "Volver"
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        24.dp
                    )
            )
        }
    }
}