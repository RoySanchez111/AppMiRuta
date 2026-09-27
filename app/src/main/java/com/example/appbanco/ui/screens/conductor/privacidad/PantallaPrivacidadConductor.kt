package com.example.appbanco.ui.screens.conductor.privacidad
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appbanco.ui.components.obtenerColoresFondo

@Composable
fun PantallaPrivacidadConductor(
    navController: NavController
) {

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    // =========================================
    // TEMA SEGÚN LA HORA
    // =========================================
    val coloresFondo = remember {
        obtenerColoresFondo()
    }

    val fondoHorario = remember(coloresFondo) {
        Brush.horizontalGradient(coloresFondo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {

        // =========================================
        // ENCABEZADO
        // =========================================
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
                    .heightIn(min = 115.dp)
                    .padding(
                        horizontal = 14.dp,
                        vertical = 18.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White,
                        modifier = Modifier.size(27.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Cuenta",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )

                    Text(
                        text = "Privacidad y seguridad",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Protección de tu información",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // =========================================
        // CONTENIDO
        // =========================================
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 16.dp,
                    vertical = 18.dp
                )
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.45f
                        )
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = primaryColor.copy(alpha = 0.12f)
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    // =================================
                    // TÍTULO EULA
                    // =================================
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.12f)
                        ) {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Column {

                            Text(
                                text = "Términos de Licencia",
                                color = onSurfaceColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )

                            Text(
                                text = "y Privacidad (EULA)",
                                color = onSurfaceColor.copy(alpha = 0.65f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    HorizontalDivider(
                        color = onSurfaceColor.copy(alpha = 0.08f)
                    )

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = "ACUERDO DE LICENCIA DE USUARIO FINAL (EULA)",
                        color = onSurfaceColor.copy(alpha = 0.75f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    SeccionPrivacidad(
                        numero = "1",
                        titulo = "Aceptación de Términos",
                        descripcion = "Al utilizar MiRuta, aceptas la recopilación y uso de tu ubicación para mostrar paradas y rutas cercanas."
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    SeccionPrivacidad(
                        numero = "2",
                        titulo = "Protección y Encriptación de Datos",
                        descripcion = "La información de la cuenta y los datos utilizados por la aplicación deben mantenerse protegidos y utilizarse únicamente para las funciones del servicio."
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    SeccionPrivacidad(
                        numero = "3",
                        titulo = "Servicios de Ubicación y GPS",
                        descripcion = "Los datos de geolocalización se procesan para apoyar la navegación, mostrar información relacionada con la ruta y mejorar la experiencia durante el servicio."
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    SeccionPrivacidad(
                        numero = "4",
                        titulo = "Licencia de Uso",
                        descripcion = "Se otorga una licencia personal para utilizar la aplicación y sus funciones de acuerdo con los términos establecidos."
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    SeccionPrivacidad(
                        numero = "5",
                        titulo = "Almacenamiento Local",
                        descripcion = "Las preferencias y la información necesaria para algunas funciones pueden almacenarse localmente en el dispositivo."
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    HorizontalDivider(
                        color = onSurfaceColor.copy(alpha = 0.08f)
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "Última actualización: Agosto 2026",
                        color = onSurfaceColor.copy(alpha = 0.55f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    // =================================
                    // BOTÓN
                    // =================================
                    Button(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {

                        Text(
                            text = "Aceptar y volver",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )
        }
    }
}

// =====================================================
// SECCIÓN DE PRIVACIDAD
// =====================================================
@Composable
private fun SeccionPrivacidad(
    numero: String,
    titulo: String,
    descripcion: String
) {

    val onSurfaceColor =
        MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {

        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(
                alpha = 0.12f
            )
        ) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = numero,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = titulo,
                color = onSurfaceColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = descripcion,
                color = onSurfaceColor.copy(alpha = 0.68f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}