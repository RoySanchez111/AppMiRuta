package com.example.appbanco.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class PreguntaFrecuente(
    val pregunta: String,
    val respuesta: String
)

val listaFaqsMock = listOf(
    PreguntaFrecuente(
        "¿Cómo rastreo mi ruta?",
        "Dirígete a la pantalla Inicio, busca tu ruta o parada en el mapa y presiona sobre ella para ver la posición y llegada estimada en tiempo real."
    ),
    PreguntaFrecuente(
        "¿Qué pasa si pierdo la señal de internet durante un viaje?",
        "Si activaste el Modo Offline en Configuración, la app seguirá mostrando el mapa y las rutas descargadas con tu última ubicación GPS."
    ),
    PreguntaFrecuente(
        "¿Por qué la app no rastrea bien mi ubicación?",
        "Asegúrate de haber otorgado los permisos de ubicación GPS precisa en los ajustes de tu dispositivo."
    ),
    PreguntaFrecuente(
        "La app se cierra sola o se congela. ¿Qué hago?",
        "Verifica que tengas la última versión de MiRuta instalada o borra la caché de la aplicación en la configuración de Android."
    ),
    PreguntaFrecuente(
        "¿Por qué la app consume tanta batería?",
        "El uso continuo del GPS y el renderizado 3D de mapas consume batería. Puedes desactivar la alta precisión o activar el ahorro de energía."
    )
)

@Composable
fun PantallaAyudaYSoporte(navController: NavController) {
    var busquedaFaq by remember { mutableStateOf("") }
    var faqExpandidaIndex by remember { mutableIntStateOf(-1) }
    var mostrarDialogoContacto by remember { mutableStateOf(false) }
    var mostrarDialogoSugerencia by remember { mutableStateOf(false) }
    var efectosActivados by remember { mutableStateOf(true) }
    var talkbackActivado by remember { mutableStateOf(true) }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    val faqsFiltradas = remember(busquedaFaq) {
        if (busquedaFaq.isBlank()) listaFaqsMock
        else listaFaqsMock.filter {
            it.pregunta.contains(busquedaFaq, ignoreCase = true) ||
            it.respuesta.contains(busquedaFaq, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(surfaceColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // BUSCADOR DE AYUDA
        OutlinedTextField(
            value = busquedaFaq,
            onValueChange = { busquedaFaq = it },
            placeholder = { Text("¿Con qué necesitas ayuda?", fontSize = 14.sp, color = onSurfaceColor.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = primaryColor) },
            trailingIcon = if (busquedaFaq.isNotEmpty()) {
                {
                    IconButton(onClick = { busquedaFaq = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedBorderColor = primaryColor.copy(alpha = 0.15f),
                focusedBorderColor = primaryColor
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SECCIÓN PREGUNTAS FRECUENTES
        Text(
            text = "Preguntas frecuentes",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF282869),
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // LISTA DE TARJETAS EXPANDIBLES
        faqsFiltradas.forEachIndexed { index, faq ->
            val esExpandida = faqExpandidaIndex == index

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        faqExpandidaIndex = if (esExpandida) -1 else index
                    }
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = faq.pregunta,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (esExpandida) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (esExpandida) "Contraer" else "Expandir",
                            tint = onSurfaceColor.copy(alpha = 0.6f)
                        )
                    }

                    if (esExpandida) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = faq.respuesta,
                            fontSize = 13.sp,
                            color = onSurfaceColor.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECCIÓN ¿NO ENCUENTRA LO QUE BUSCABA?
        Text(
            text = "¿No encuentra lo que buscaba?",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFFF26E68),
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // FILA DE BOTONES GRANDES (CONTACTO Y SUGERENCIAS)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // BOTÓN CONTACTO (Coral/Rosa)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFADBD8),
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        mostrarDialogoContacto = true
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color(0xFFE74C3C),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Contacto",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFE74C3C)
                    )
                }
            }

            // BOTÓN SUGERENCIAS (Azul Suave)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFD4E6F1),
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        mostrarDialogoSugerencia = true
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = Color(0xFF2980B9),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sugerencias",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF2980B9)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ACCESIBILIDAD Y EFECTOS
        Text(
            text = "Ajustes de Accesibilidad",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = onSurfaceColor.copy(alpha = 0.8f),
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Efectos de pantalla", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Animaciones y efectos visuales", fontSize = 11.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = efectosActivados,
                        onCheckedChange = { efectosActivados = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                    )
                }
                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.08f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Asistente TalkBack", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Lectura de voz y etiquetas de accesibilidad", fontSize = 11.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = talkbackActivado,
                        onCheckedChange = { talkbackActivado = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // DIÁLOGO CONTACTO DIRECTO
    if (mostrarDialogoContacto) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoContacto = false },
            title = { Text("Contacto de Soporte", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("📞 Teléfono: 800-MIRUTA (800-647882)")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("✉️ Correo: soporte@miruta.app")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("⏰ Atención activa 24 horas, 7 días a la semana.")
                }
            },
            confirmButton = {
                Button(onClick = {
                    Toast.makeText(context, "Conectando con un agente de soporte...", Toast.LENGTH_SHORT).show()
                    mostrarDialogoContacto = false
                }) {
                    Text("Llamar Ahora")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoContacto = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // DIÁLOGO ENVIAR SUGERENCIA
    if (mostrarDialogoSugerencia) {
        var textoSugerencia by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { mostrarDialogoSugerencia = false },
            title = { Text("Buzón de Sugerencias", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Escribe tu sugerencia o reporte para mejorar MiRuta:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = textoSugerencia,
                        onValueChange = { textoSugerencia = it },
                        label = { Text("Tu sugerencia") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (textoSugerencia.isNotBlank()) {
                        Toast.makeText(context, "¡Gracias! Tu sugerencia ha sido enviada.", Toast.LENGTH_SHORT).show()
                        mostrarDialogoSugerencia = false
                    }
                }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoSugerencia = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
