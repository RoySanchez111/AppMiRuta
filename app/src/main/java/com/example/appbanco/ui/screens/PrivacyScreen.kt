package com.example.appbanco.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PantallaPrivacidad(navController: NavController) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(surfaceColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = primaryColor, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Términos de Licencia y Privacidad (EULA)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = onSurfaceColor,
                        modifier = Modifier.semantics { heading() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ACUERDO DE LICENCIA DE USUARIO FINAL (EULA)\n\n" +
                            "1. Aceptación de Términos: Al utilizar MiRuta, aceptas la recopilación y uso de tu ubicación para mostrar paradas y rutas cercanas en tiempo real.\n\n" +
                            "2. Protección y Encriptación de Datos: Las contraseñas se almacenan mediante algoritmos de encriptación segura (BCrypt con sal dinámica) en una base de datos local SQLite (Room) en tu dispositivo.\n\n" +
                            "3. Servicios de Ubicación y GPS: Los datos de geolocalización se procesan en tiempo real únicamente para la navegación y no son compartidos con terceros sin tu consentimiento.\n\n" +
                            "4. Licencia de Uso: Se te otorga una licencia personal, no exclusiva e intransferible para usar la aplicación en dispositivos Android compatibles.\n\n" +
                            "5. Almacenamiento Local: Las preferencias de sesión y mapas descargados en modo offline se almacenan localmente en DataStore seguro.\n\n" +
                            "Última actualización: Agosto 2026",
                    fontSize = 13.sp,
                    color = onSurfaceColor.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Aceptar y Volver", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
