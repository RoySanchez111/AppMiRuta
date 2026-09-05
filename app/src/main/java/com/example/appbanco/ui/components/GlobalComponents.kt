package com.example.appbanco.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appbanco.data.AlertaIncidencia
import com.example.appbanco.data.LineaRuta
import com.example.appbanco.logic.obtenerMensajeBienvenida
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import androidx.navigation.NavController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

@Composable
fun BarraNavegacionInferior(navController: NavController, rutaActual: String?) {
    val themeColor = MaterialTheme.colorScheme.onSurface
    val containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)

    NavigationBar(
        containerColor = containerColor,
        contentColor = themeColor
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
                    unselectedIconColor = themeColor.copy(alpha = 0.6f), 
                    unselectedTextColor = themeColor.copy(alpha = 0.6f), 
                    indicatorColor = Color(0xFF00BCD4).copy(alpha = 0.1f)
                ),
                onClick = { 
                    if (rutaActual != ruta) {
                        navController.navigate(ruta) { 
                            popUpTo("principal") { 
                                saveState = true
                                inclusive = false
                            }
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
fun EncabezadoGlobal(
    titulo: String, 
    subtitulo: String? = null,
    onBackClick: (() -> Unit)? = null,
    onProfileClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)),
        color = Color.Transparent
    ) {
        TimeBasedBackground(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 24.dp, bottom = 16.dp, top = 44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(start = if (onBackClick == null) 8.dp else 0.dp)
                            .semantics { heading() }
                    ) {
                        if (subtitulo != null) {
                            Text(
                                text = subtitulo,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Text(
                            text = titulo,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }
                
                // Avatar
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable { onProfileClick() }
                        .semantics {
                            role = Role.Button
                            contentDescription = "Ir a mi perfil"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalistAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "minimalist")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics {
            liveRegion = LiveRegionMode.Polite
            contentDescription = "Buscando ubicación actual"
        }
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .alpha(alpha),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Buscando ubicación...", 
            fontSize = 18.sp, 
            color = MaterialTheme.colorScheme.onBackground, 
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LineCard(linea: LineaRuta, scope: CoroutineScope, snackbarHostState: SnackbarHostState) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "Línea ${linea.numero}, ${linea.nombre}, ${linea.destino}, frecuencia ${linea.frecuencia}"
            }
            .clickable {
                scope.launch {
                    snackbarHostState.showSnackbar("Seleccionaste: ${linea.nombre}")
                }
            },
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
                        color = onSurfaceColor,
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
                    color = onSurfaceColor.copy(alpha = 0.7f),
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
                    contentDescription = null,
                    tint = onSurfaceColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AlertCard(alerta: AlertaIncidencia) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Alerta ${alerta.tipo} en ruta ${alerta.ruta}: ${alerta.titulo}. ${alerta.descripcion}. Hace ${alerta.tiempo}"
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = alerta.tipoColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = alerta.tipo,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(alerta.rutaColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = alerta.ruta,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(alerta.iconoContainerColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = alerta.icono,
                        contentDescription = null,
                        tint = alerta.tipoColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = alerta.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = onSurfaceColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = alerta.descripcion,
                        fontSize = 13.sp,
                        color = onSurfaceColor.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = alerta.tiempo,
                fontSize = 12.sp,
                color = onSurfaceColor.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}

@Composable
fun OpcionCuenta(
    texto: String, 
    subtexto: String? = null, 
    onClick: () -> Unit = {}
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = if (subtexto != null) "$texto: $subtexto" else texto
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = onSurfaceColor.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = texto,
                    fontSize = 12.sp,
                    color = onSurfaceColor.copy(alpha = 0.8f)
                )
                if (subtexto != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "($subtexto)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = onSurfaceColor.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
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
            Icon(icono, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))
            Text(titulo, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        }
    }
}
