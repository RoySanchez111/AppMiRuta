package com.example.appbanco.tutorial

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appbanco.R
import com.example.appbanco.logic.SessionManager
import kotlinx.coroutines.launch

data class PaginaTutorial(
    val titulo: String,
    val descripcion: String,
    val iconoPrincipal: ImageVector,
    val iconoSecundario: ImageVector?,
    val textoBoton: String
)

val paginasTutorial = listOf(
    PaginaTutorial(
        titulo = "Encuentra la mejor ruta",
        descripcion = "Explora los horarios en vivo, líneas disponibles y paradas en tiempo real para moverte por la ciudad sin complicaciones.",
        iconoPrincipal = Icons.Default.DirectionsBus,
        iconoSecundario = Icons.Default.Place,
        textoBoton = "Siguiente >"
    ),
    PaginaTutorial(
        titulo = "Horarios en tiempo real",
        descripcion = "Consulta la llegada exacta de autobuses y conoce retrasos o desvíos antes de salir de casa.",
        iconoPrincipal = Icons.Default.Schedule,
        iconoSecundario = Icons.Default.Map,
        textoBoton = "Siguiente >"
    ),
    PaginaTutorial(
        titulo = "Notificaciones y Alertas",
        descripcion = "Recibe avisos inmediatos del servicio y reporta incidencias para ayudar a toda la comunidad de viajeros.",
        iconoPrincipal = Icons.Default.NotificationsActive,
        iconoSecundario = Icons.Default.CheckCircle,
        textoBoton = "Comenzar a usar MiRuta"
    )
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PantallaTutorial(
    navController: NavController,
    sessionManager: SessionManager,
    onFinalizarTutorial: () -> Unit = {}
) {
    var paginaActual by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val pagina = paginasTutorial[paginaActual]

    // Fondo degradado coral/rosa a naranja cálido idéntico al de la imagen
    val fondoDegradado = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF5252), // Coral brillante
                Color(0xFFFF7A59), // Rosa anaranjado
                Color(0xFFFF9E80)  // Naranja suave
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoDegradado)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ENCABEZADO SUPERIOR: ¡Hola! Te damos la bienvenida a MiRuta
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.semantics { heading() }
                ) {
                    Text(
                        text = "¡Hola! Te damos la\nbienvenida a ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_miruta_texto),
                        contentDescription = "MiRuta",
                        modifier = Modifier
                            .width(100.dp)
                            .height(30.dp)
                    )
                }
            }

            // ILUSTRACIÓN CENTRAL: Tarjeta gris redondeada con icono de Autobús y Ubicación
            AnimatedContent(
                targetState = pagina,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "animacionTutorial"
            ) { pag ->
                Surface(
                    modifier = Modifier
                        .size(220.dp)
                        .padding(12.dp),
                    shape = RoundedCornerShape(48.dp),
                    color = Color(0xFFE2E2E2), // Gris claro idéntico a la maqueta
                    shadowElevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Icono de Autobús
                        Icon(
                            imageVector = pag.iconoPrincipal,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(110.dp)
                        )

                        // Icono secundario en la esquina superior derecha (Pin de ubicación / Mapa)
                        if (pag.iconoSecundario != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = pag.iconoSecundario,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            }

            // TARJETA INFERIOR: Beige/Crema redondeada con texto y botón
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                color = Color(0xFFEAE0D5), // Tono beige/crema suave idéntico
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TÍTULO DE LA PÁGINA
                    Text(
                        text = pagina.titulo,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // DESCRIPCIÓN DE LA PÁGINA
                    Text(
                        text = pagina.descripcion,
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // INDICADOR DE PÁGINAS (PUNTOS)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        paginasTutorial.indices.forEach { index ->
                            val esActivo = index == paginaActual
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(if (esActivo) 24.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (esActivo) Color.Black else Color.Black.copy(alpha = 0.2f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // BOTÓN OVALADO SIGUIENTE
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = pagina.textoBoton
                            }
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (paginaActual < paginasTutorial.size - 1) {
                                    paginaActual++
                                } else {
                                    // Finalizar tutorial y guardar preferencia en DataStore
                                    scope.launch {
                                        sessionManager.setTutorialCompleted(true)
                                        onFinalizarTutorial()
                                        navController.navigate("principal") {
                                            popUpTo("tutorial") { inclusive = true }
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                }
                            },
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFDCD2C7), // Tono del botón ovalado
                        border = BorderStroke(1.dp, Color(0xFFC4B8A9))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pagina.textoBoton,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
