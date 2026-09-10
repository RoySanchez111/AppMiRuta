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
import com.example.appbanco.ui.components.obtenerColoresFondo
import kotlinx.coroutines.launch

data class PaginaTutorial(
    val titulo: String,
    val descripcion: String,
    val drawableResId: Int,
    val textoBoton: String
)

val paginasTutorial = listOf(
    PaginaTutorial(
        titulo = "Encuentra la mejor ruta",
        descripcion = "Descubre las rutas y líneas de transporte público más eficientes en Puebla. Consulta paradas cercanas, traza tu itinerario y llega siempre a tiempo a tu destino.",
        drawableResId = R.drawable.ic_tutorial_bus_vector,
        textoBoton = "Siguiente >"
    ),
    PaginaTutorial(
        titulo = "Horarios en tiempo real",
        descripcion = "Consulta la llegada exacta de autobuses y conoce retrasos o desvíos antes de salir de casa.",
        drawableResId = R.drawable.ic_tutorial_map_vector,
        textoBoton = "Siguiente >"
    ),
    PaginaTutorial(
        titulo = "Notificaciones y Alertas",
        descripcion = "Recibe avisos inmediatos del servicio y reporta incidencias para ayudar a toda la comunidad de viajeros.",
        drawableResId = R.drawable.ic_tutorial_warning_vector,
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

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    // Fondo degradado dinámico según la hora del día (Madrugada, Mañana, Atardecer, Ocaso, Noche)
    val coloresFondo = remember { obtenerColoresFondo() }
    val fondoDegradado = remember(coloresFondo) { Brush.verticalGradient(coloresFondo) }

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
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 24.dp, end = 24.dp)
            ) {
                Text(
                    text = "¡Hola! Te damos la\nbienvenida a",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp,
                    modifier = Modifier.semantics { heading() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.ic_miruta_texto),
                    contentDescription = "MiRuta",
                    modifier = Modifier
                        .width(140.dp)
                        .height(40.dp)
                )
            }

            // ILUSTRACIÓN CENTRAL: Tarjeta gris redondeada con icono vectorial
            AnimatedContent(
                targetState = pagina,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "animacionTutorial"
            ) { pag ->
                Surface(
                    modifier = Modifier
                        .size(230.dp)
                        .padding(12.dp),
                    shape = RoundedCornerShape(48.dp),
                    color = Color(0xFFE2E2E2), // Gris claro idéntico a la maqueta
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = pag.drawableResId),
                            contentDescription = pag.titulo,
                            modifier = Modifier.size(160.dp)
                        )
                    }
                }
            }

            // TARJETA INFERIOR: Redondeada con texto y botón adaptado al tema del día
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                color = surfaceColor,
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
                        color = onSurfaceColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // DESCRIPCIÓN DE LA PÁGINA
                    Text(
                        text = pagina.descripcion,
                        fontSize = 14.sp,
                        color = onSurfaceColor.copy(alpha = 0.8f),
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
                                    .background(if (esActivo) primaryColor else onSurfaceColor.copy(alpha = 0.2f))
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
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pagina.textoBoton,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                        }
                    }
                }
            }
        }
    }
}
