package com.example.appbanco.ui.screens.conductor.perfil

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appbanco.logic.SessionManager
import com.example.appbanco.ui.components.obtenerColoresFondo
import com.example.appbanco.ui.screens.conductor.components.BarraNavegacionConductor
import com.example.appbanco.ui.screens.conductor.perfil.components.FichaUnidad
import com.example.appbanco.ui.screens.conductor.perfil.components.OpcionesAccesibilidad
import kotlinx.coroutines.launch

@Composable
fun PantallaPerfilConductor(
    navController: NavController
) {

    val context = LocalContext.current

    // Sesion
    val sessionManager = remember {
        SessionManager(context)
    }

    val coroutineScope = rememberCoroutineScope()

    // Ruta actual
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    // Temas
    val coloresFondo = remember {
        obtenerColoresFondo()
    }

    val fondoHorario = remember(coloresFondo) {
        Brush.horizontalGradient(coloresFondo)
    }

    // Por ahora mantenemos el conductor de prueba
    val nombreConductor = "Rafita"
    val correoConductor = "rafita@miruta.com"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Encabezado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = fondoHorario,
                    shape = RoundedCornerShape(
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .heightIn(min = 120.dp)
                    .padding(
                        horizontal = 22.dp,
                        vertical = 20.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Informacion
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Cuenta",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = "Mi perfil",
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 30.sp
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = "Información del conductor",
                        color = Color.White.copy(alpha = 0.80f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(
                    modifier = Modifier.width(16.dp)
                )

                // avatar del encabezado
                Surface(
                    modifier = Modifier.size(62.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                ) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = nombreConductor
                                .firstOrNull()
                                ?.uppercase()
                                ?: "C",
                            color = Color.White,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ==========================================
        // CONTENIDO
        // ==========================================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 16.dp,
                    vertical = 18.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ======================================
            // DATOS DEL CONDUCTOR
            // ======================================
            Text(
                text = nombreConductor,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = correoConductor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = 0.6f
                )
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.12f
                )
            ) {

                Text(
                    text = "Conductor",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 5.dp
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // ======================================
            // FICHA DE LA UNIDAD
            // ======================================
            FichaUnidad()

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // FINALIZAR TURNO
            Button(
                onClick = {
                    Toast.makeText(
                        context,
                        "Turno finalizado",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD20A0A)
                )
            ) {

                Icon(
                    imageVector = Icons.Outlined.PowerSettingsNew,
                    contentDescription = null,
                    tint = Color.White
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Finalizar turno",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            // Accesibilidad
            OpcionesAccesibilidad()

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // Opciones cuenta :V
            Text(
                text = "Cuenta",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OpcionCuentaConductor(
                titulo = "Configuración",
                descripcion = "Preferencias de la aplicación",
                icono = {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null
                    )
                },
                onClick = {
                    navController.navigate("configuracion_conductor")
                }
            )

            OpcionCuentaConductor(
                titulo = "Privacidad y seguridad",
                descripcion = "Datos, permisos y seguridad",
                icono = {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null
                    )
                },
                onClick = {
                    navController.navigate("privacidad_conductor")
                }
            )

            OpcionCuentaConductor(
                titulo = "Ayuda y soporte",
                descripcion = "Preguntas frecuentes y contacto",
                icono = {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = null
                    )
                },
                onClick = {
                    navController.navigate("ayuda_conductor")
                }
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )
            // cerrar sesion
            Button(
                onClick = {

                    coroutineScope.launch {

                        sessionManager.logout()

                        navController.navigate("login") {
                            popUpTo(0) {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD20A0A)
                )
            ) {

                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = "Cerrar sesión",
                    tint = Color.White
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Cerrar sesión",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        // Navegacion
        BarraNavegacionConductor(
            navController = navController,
            rutaActual = rutaActual
        )
    }
}

// opcion cuenta
@Composable
private fun OpcionCuentaConductor(
    titulo: String,
    descripcion: String,
    icono: @Composable () -> Unit,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icono
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.12f
                )
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                        icono()
                }
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            // Textos
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = titulo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = descripcion,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.55f
                    )
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.5f
                )
            )
        }
    }
}