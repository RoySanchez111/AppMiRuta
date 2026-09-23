package com.example.appbanco.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PantallaPrivacidad(navController: NavController) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var ubicacionSegundoPlano by remember { mutableStateOf(true) }
    var guardarHistorial by remember { mutableStateOf(true) }
    var verAnuncios by remember { mutableStateOf(false) }
    var verificacionDosPasos by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.TopCenter
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 850.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // SECCIÓN: DATOS Y UBICACIÓN
                Text(
                    text = "Datos y ubicación",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        OpcionPrivacidadSwitch(
                            titulo = "Ubicación en segundo plano",
                            descripcion = "Necesaria para rastreo GPS y llegada de autobuses",
                            activado = ubicacionSegundoPlano,
                            onCambio = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                ubicacionSegundoPlano = it
                                Toast.makeText(context, if (it) "Ubicación en 2do plano activada" else "Ubicación en 2do plano desactivada", Toast.LENGTH_SHORT).show()
                            }
                        )

                        OpcionPrivacidadSwitch(
                            titulo = "Guardar historial de viajes",
                            descripcion = "Registro local optimizado (se borra cada 30 días)",
                            activado = guardarHistorial,
                            onCambio = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                guardarHistorial = it
                            }
                        )

                        OpcionPrivacidadSwitch(
                            titulo = "Personalización de anuncios",
                            descripcion = "Ayuda a financiar el mantenimiento gratuito de la app",
                            activado = verAnuncios,
                            onCambio = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                verAnuncios = it
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // SECCIÓN: SEGURIDAD
                Text(
                    text = "Seguridad",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        OpcionPrivacidadSwitch(
                            titulo = "Verificación en 2 pasos",
                            descripcion = "Protección adicional de cuenta mediante credenciales seguras",
                            activado = verificacionDosPasos,
                            onCambio = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                verificacionDosPasos = it
                                Toast.makeText(context, if (it) "Verificación en 2 pasos habilitada" else "Verificación en 2 pasos deshabilitada", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    Toast.makeText(context, "Configurar contraseña de acceso", Toast.LENGTH_SHORT).show()
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f).padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "Establecer contraseña de acceso",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Contraseña local adicional para asegurar la app",
                                    fontSize = 11.sp,
                                    color = onSurfaceColor.copy(alpha = 0.6f)
                                )
                            }

                            TextButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    Toast.makeText(context, "Establecer contraseña", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(
                                    text = "Establecer",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // SECCIÓN: TUS DATOS
                Text(
                    text = "Tus datos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "🧹 Historial de viajes borrado exitosamente", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = onSurfaceColor.copy(alpha = 0.05f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = "Borrar historial de viajes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "⚠️ Acción restringida por seguridad", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = onSurfaceColor.copy(alpha = 0.05f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = "Eliminar cuenta permanentemente",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // AVISO DE PRIVACIDAD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = onSurfaceColor.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.08f))
                ) {
                    TextButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            Toast.makeText(context, "📄 Abriendo Aviso de Privacidad oficial...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Consultar el ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = onSurfaceColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Aviso de Privacidad",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
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
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Switch
                contentDescription = "$titulo, $descripcion, ${if (activado) "Activado" else "Desactivado"}"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = descripcion,
                fontSize = 11.sp,
                color = onSurfaceColor.copy(alpha = 0.6f)
            )
        }

        Switch(
            checked = activado,
            onCheckedChange = onCambio,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primaryColor,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = onSurfaceColor.copy(alpha = 0.25f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
