package com.example.appbanco.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appbanco.ui.components.TimeBasedBackground

@Composable
fun PantallaPrivacidad(navController: NavController) {

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val cardColor = MaterialTheme.colorScheme.surface
    val coralColor = Color(0xFFF26767)
    val borderColor = onSurfaceColor.copy(alpha = 0.12f)
    val textMutedColor = onSurfaceColor.copy(alpha = 0.6f)

    var ubicacionSegundoPlano by remember {
        mutableStateOf(true)
    }

    var guardarHistorial by remember {
        mutableStateOf(true)
    }

    var verAnuncios by remember {
        mutableStateOf(false)
    }

    var verificacionDosPasos by remember {
        mutableStateOf(false)
    }

    TimeBasedBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // DATOS Y UBICACIÓN
            Text(
                text = "Datos y ubicación",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textMutedColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
                ) {
                    OpcionPrivacidadSwitch(
                        titulo = "Ubicación en segundo plano",
                        descripcion = "Necesaria para GPS",
                        activado = ubicacionSegundoPlano,
                        onCambio = {
                            ubicacionSegundoPlano = it
                        }
                    )

                    OpcionPrivacidadSwitch(
                        titulo = "Guardar historial de viajes",
                        descripcion = "Se borra cada 30 días",
                        activado = guardarHistorial,
                        onCambio = {
                            guardarHistorial = it
                        }
                    )

                    OpcionPrivacidadSwitch(
                        titulo = "Ver anuncios",
                        descripcion = "Anuncios que financian la app",
                        activado = verAnuncios,
                        onCambio = {
                            verAnuncios = it
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Seguridad",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textMutedColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
                ) {
                    OpcionPrivacidadSwitch(
                        titulo = "Verificación en 2 pasos",
                        descripcion = "Sin datos",
                        activado = verificacionDosPasos,
                        onCambio = {
                            verificacionDosPasos = it
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Establecer contraseña",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = onSurfaceColor
                            )

                            Text(
                                text = "Contraseña para acceder a la app",
                                fontSize = 10.sp,
                                color = textMutedColor
                            )
                        }

                        TextButton(
                            onClick = {},
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Establecer",
                                fontSize = 12.sp,
                                color = coralColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tus datos",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textMutedColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = cardColor,
                    contentColor = coralColor
                )
            ) {
                Text(
                    text = "Borrar historial de viajes",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = cardColor,
                    contentColor = coralColor
                )
            ) {
                Text(
                    text = "Eliminar cuenta",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AVISO DE PRIVACIDAD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor)
            ) {
                TextButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Consultar el ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = textMutedColor
                    )

                    Text(
                        text = "Aviso de Privacidad",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = coralColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OpcionPrivacidadSwitch(
    titulo: String,
    descripcion: String,
    activado: Boolean,
    onCambio: (Boolean) -> Unit
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = titulo,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = onSurfaceColor
            )

            Text(
                text = descripcion,
                fontSize = 10.sp,
                color = onSurfaceColor.copy(alpha = 0.6f)
            )
        }

        Switch(
            checked = activado,
            onCheckedChange = onCambio,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFF26767),
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = onSurfaceColor.copy(alpha = 0.25f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}