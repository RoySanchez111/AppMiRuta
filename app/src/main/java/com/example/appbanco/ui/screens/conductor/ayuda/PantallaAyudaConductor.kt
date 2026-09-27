package com.example.appbanco.ui.screens.conductor.ayuda
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appbanco.ui.components.obtenerColoresFondo

@Composable
fun PantallaAyudaConductor(
    navController: NavController
) {

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    var busqueda by remember {
        mutableStateOf("")
    }

    // =========================================
    // TEMA SEGÚN LA HORA
    // =========================================
    val coloresFondo = remember {
        obtenerColoresFondo()
    }

    val fondoHorario = remember(coloresFondo) {
        Brush.horizontalGradient(coloresFondo)
    }

    val preguntas = remember {
        listOf(
            PreguntaAyuda(
                pregunta = "¿Cómo reporto un incidente durante mi ruta?",
                respuesta = "Ingresa a la sección de Alertas y utiliza la opción para reportar una incidencia. Selecciona el tipo de incidente y registra la información necesaria."
            ),
            PreguntaAyuda(
                pregunta = "¿Cómo funciona el modo offline?",
                respuesta = "El modo offline permite conservar información descargada previamente para consultar datos básicos de tus rutas cuando la conexión sea limitada."
            ),
            PreguntaAyuda(
                pregunta = "¿Cómo puedo actualizar mis rutas descargadas?",
                respuesta = "Desde Configuración, entra a la sección Viajes y selecciona Actualizar en la opción Rutas descargadas."
            ),
            PreguntaAyuda(
                pregunta = "¿Qué hago si el GPS no muestra mi ubicación?",
                respuesta = "Comprueba que la ubicación del dispositivo esté activada y que MiRuta tenga permiso para utilizarla. Después vuelve a la pantalla de inicio."
            ),
            PreguntaAyuda(
                pregunta = "¿Cómo cambio la apariencia de la aplicación?",
                respuesta = "En Configuración encontrarás la sección Apariencia, donde puedes seleccionar Claro, Oscuro o Auto."
            )
        )
    }

    val preguntasFiltradas = remember(busqueda, preguntas) {

        if (busqueda.isBlank()) {
            preguntas
        } else {
            preguntas.filter {
                it.pregunta.contains(
                    busqueda,
                    ignoreCase = true
                ) ||
                        it.respuesta.contains(
                            busqueda,
                            ignoreCase = true
                        )
            }
        }
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
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White,
                        modifier = Modifier.size(27.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Column {

                    Text(
                        text = "Cuenta",
                        color = Color.White.copy(
                            alpha = 0.85f
                        ),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )

                    Text(
                        text = "Ayuda y soporte",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "¿En qué podemos ayudarte?",
                        color = Color.White.copy(
                            alpha = 0.8f
                        ),
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
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 18.dp
                )
        ) {

            // =====================================
            // BUSCADOR
            // =====================================
            OutlinedTextField(
                value = busqueda,
                onValueChange = {
                    busqueda = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Buscar en ayuda..."
                    )
                },
                leadingIcon = {

                    Icon(
                        imageVector =
                            Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {

                    if (busqueda.isNotEmpty()) {

                        IconButton(
                            onClick = {
                                busqueda = ""
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Close,
                                contentDescription =
                                    "Limpiar búsqueda"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {}
                )
            )

            Spacer(
                modifier = Modifier.height(26.dp)
            )

            // =====================================
            // PREGUNTAS FRECUENTES
            // =====================================
            Text(
                text = "Preguntas frecuentes",
                color = onSurfaceColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = "Encuentra respuestas rápidas a las dudas más comunes.",
                color = onSurfaceColor.copy(
                    alpha = 0.6f
                ),
                fontSize = 12.sp
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (preguntasFiltradas.isEmpty()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme
                                .surfaceVariant
                                .copy(alpha = 0.45f)
                    )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = onSurfaceColor.copy(
                                alpha = 0.45f
                            ),
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "No encontramos resultados",
                            color = onSurfaceColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "Intenta con otra búsqueda.",
                            color = onSurfaceColor.copy(
                                alpha = 0.55f
                            ),
                            fontSize = 12.sp
                        )
                    }
                }

            } else {

                preguntasFiltradas.forEach { item ->

                    TarjetaPreguntaConductor(
                        pregunta = item.pregunta,
                        respuesta = item.respuesta
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // =====================================
            // MÁS AYUDA
            // =====================================
            Text(
                text = "¿Necesitas más ayuda?",
                color = onSurfaceColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // CONTACTO
            TarjetaSoporteConductor(
                icono = Icons.Default.SupportAgent,
                titulo = "Contacto",
                descripcion = "Comunícate con el equipo de soporte.",
                textoBoton = "Contactar",
                onClick = {
                    // Aquí podemos conectar soporte después
                }
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // SUGERENCIAS
            TarjetaSoporteConductor(
                icono = Icons.Default.Lightbulb,
                titulo = "Sugerencias",
                descripcion = "Ayúdanos a mejorar la experiencia de MiRuta.",
                textoBoton = "Enviar sugerencia",
                onClick = {
                    // Aquí podemos conectar sugerencias después
                }
            )

            Spacer(
                modifier = Modifier.height(30.dp)
            )
        }
    }
}

// =====================================================
// MODELO DE PREGUNTA
// =====================================================
private data class PreguntaAyuda(
    val pregunta: String,
    val respuesta: String
)

// =====================================================
// PREGUNTA DESPLEGABLE
// =====================================================
@Composable
private fun TarjetaPreguntaConductor(
    pregunta: String,
    respuesta: String
) {

    var expandida by remember {
        mutableStateOf(false)
    }

    val onSurfaceColor =
        MaterialTheme.colorScheme.onSurface

    val primaryColor =
        MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                expandida = !expandida
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme
                    .surfaceVariant
                    .copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = primaryColor.copy(
                alpha = 0.10f
            )
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = pregunta,
                    modifier = Modifier.weight(1f),
                    color = onSurfaceColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 19.sp
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Icon(
                    imageVector =
                        if (expandida)
                            Icons.Default.ExpandLess
                        else
                            Icons.Default.ExpandMore,
                    contentDescription =
                        if (expandida)
                            "Contraer"
                        else
                            "Expandir",
                    tint = primaryColor
                )
            }

            AnimatedVisibility(
                visible = expandida
            ) {

                Column {

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    HorizontalDivider(
                        color = onSurfaceColor.copy(
                            alpha = 0.08f
                        )
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text = respuesta,
                        color = onSurfaceColor.copy(
                            alpha = 0.68f
                        ),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// =====================================================
// TARJETA CONTACTO / SUGERENCIAS
// =====================================================
@Composable
private fun TarjetaSoporteConductor(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    descripcion: String,
    textoBoton: String,
    onClick: () -> Unit
) {

    val onSurfaceColor =
        MaterialTheme.colorScheme.onSurface

    val primaryColor =
        MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme
                    .surfaceVariant
                    .copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = primaryColor.copy(
                alpha = 0.12f
            )
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = primaryColor.copy(
                    alpha = 0.12f
                )
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = titulo,
                    color = onSurfaceColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = descripcion,
                    color = onSurfaceColor.copy(
                        alpha = 0.6f
                    ),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            TextButton(
                onClick = onClick
            ) {

                Text(
                    text = textoBoton,
                    color = primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}