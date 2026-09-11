package com.example.appbanco.ui.screens
import androidx.compose.foundation.background
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
@Composable
fun PantallaPrivacidad(navController: NavController) {

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(surfaceColor)
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Datos y ubicación",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = onSurfaceColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
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
            color = onSurfaceColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
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
                            color = onSurfaceColor.copy(alpha = 0.6f)
                        )
                    }

                    TextButton(
                        onClick = {
                        },
                        contentPadding = PaddingValues(
                            horizontal = 4.dp,
                            vertical = 0.dp
                        )
                    ) {
                        Text(
                            text = "Establecer",
                            fontSize = 10.sp,
                            color = Color(0xFFF26767)
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
            color = onSurfaceColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                // Después agregaremos la confirmación
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = Color(0xFFF26767)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp
            )
        ) {
            Text(
                text = "Borrar historial de viajes",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                // Después agregaremos la confirmación
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = Color(0xFFF26767)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp
            )
        ) {
            Text(
                text = "Eliminar cuenta",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Aquí conectaremos el aviso de privacidad
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = onSurfaceColor
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp
            )
        ) {
            Text(
                text = "Consultar el ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )

            Text(
                text = "Aviso de Privacidad",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFF26767)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OpcionPrivacidadSwitch(
    titulo: String,
    descripcion: String,
    activado: Boolean,
    onCambio: (Boolean) -> Unit
) {
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
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = descripcion,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.6f
                )
            )
        }

        Switch(
            checked = activado,
            onCheckedChange = onCambio,
            modifier = Modifier
                .width(44.dp)
                .height(26.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFF26767),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor =
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            )
        )
    }
}