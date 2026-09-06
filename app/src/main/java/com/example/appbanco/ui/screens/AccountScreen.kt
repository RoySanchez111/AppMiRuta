package com.example.appbanco.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.appbanco.data.database.UserDao
import com.example.appbanco.logic.SessionManager
import com.example.appbanco.ui.components.FotoPerfilAvatar
import com.example.appbanco.ui.components.OpcionCuenta
import com.example.appbanco.ui.viewmodel.MainViewModel
import com.example.appbanco.ui.viewmodel.RutaFrecuenteItem
import kotlinx.coroutines.launch

@Composable
fun PantallaCuenta(
    sessionManager: SessionManager, 
    navController: NavController, 
    viewModel: MainViewModel,
    userDao: UserDao
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onBackground = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    val context = LocalContext.current
    var modoOffline by remember { mutableStateOf(true) }
    var mostrarDialogoTema by remember { mutableStateOf(false) }
    var mostrarDialogoNombre by remember { mutableStateOf(false) }
    var mostrarDialogoPrivacidad by remember { mutableStateOf(false) }
    var mostrarDialogoAyuda by remember { mutableStateOf(false) }
    var mostrarDialogoAgregarRuta by remember { mutableStateOf(false) }
    var rutaSeleccionadaOpciones by remember { mutableStateOf<RutaFrecuenteItem?>(null) }
    var efectosPantallaActivados by remember { mutableStateOf(true) }
    var talkbackActivado by remember { mutableStateOf(true) }
    var mostrarDialogoFoto by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val usuarioActual = viewModel.usuarioActual.value
    val inicialUsuario = if (usuarioActual.isNotBlank()) usuarioActual.take(1).uppercase() else "U"
    val fotoUri = viewModel.fotoPerfilUri.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(surfaceColor)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Usuario $usuarioActual. Toca la foto para cambiarla."
                }, 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clickable { mostrarDialogoFoto = true },
                contentAlignment = Alignment.BottomEnd
            ) {
                FotoPerfilAvatar(
                    fotoUri = fotoUri,
                    inicialNombre = inicialUsuario,
                    tamanoDp = 60,
                    onFotoClick = { mostrarDialogoFoto = true }
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Cambiar foto de perfil",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(usuarioActual, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = onSurface)
                Text("Usuario registrado • Toca para editar foto", fontSize = 12.sp, color = onSurface.copy(alpha = 0.6f))
            }
            IconButton(
                onClick = { mostrarDialogoNombre = true },
                modifier = Modifier.semantics {
                    role = Role.Button
                    contentDescription = "Editar nombre de usuario"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar nombre",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Rutas frecuentes", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                TextButton(
                    onClick = { mostrarDialogoAgregarRuta = true },
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "Agregar nueva ruta frecuente"
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            viewModel.listaRutasFrecuentes.chunked(2).forEach { parRutas ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    parRutas.forEach { ruta ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(84.dp)
                                .semantics(mergeDescendants = true) {
                                    role = Role.Button
                                    contentDescription = "Ruta frecuente ${ruta.nombre}, ${ruta.ubicacion}"
                                }
                                .clickable {
                                    rutaSeleccionadaOpciones = ruta
                                }, 
                            shape = RoundedCornerShape(16.dp), 
                            color = ruta.color
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp), 
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) { 
                                    Icon(ruta.icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(ruta.nombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) 
                                }
                                Text(ruta.ubicacion, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                    if (parRutas.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Text(
                "Modo Offline", 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Bold, 
                color = onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        role = Role.Switch
                        contentDescription = "Modo Offline, Habilitar descarga de mapas: ${if (modoOffline) "Activado" else "Desactivado"}"
                    }, 
                shape = RoundedCornerShape(18.dp), 
                color = onSurface.copy(alpha = 0.05f)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Habilitar descarga de mapas", fontSize = 13.sp, color = onSurface.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
                    Switch(checked = modoOffline, onCheckedChange = { modoOffline = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFEB30F)))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), shape = RoundedCornerShape(24.dp), color = onBackground.copy(alpha = 0.03f)) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OpcionCuenta("Configuración", onClick = { navController.navigate("configuracion") })
                OpcionCuenta(
                    texto = "Tema de la APP", 
                    subtexto = viewModel.modoTema.value,
                    onClick = { mostrarDialogoTema = true }
                )
                OpcionCuenta("Privacidad y Seguridad", onClick = { navController.navigate("privacidad") })
                OpcionCuenta(
                    texto = "Ayuda y Soporte", 
                    subtexto = if (efectosPantallaActivados) "Efectos ON" else "Efectos OFF",
                    onClick = { navController.navigate("ayuda") }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    sessionManager.logout()
                    navController.navigate("login") {
                        popUpTo("principal") { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 4.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Cerrar sesión"
                },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (mostrarDialogoAgregarRuta) {
        DialogoAgregarRutaFrecuente(
            onDismiss = { mostrarDialogoAgregarRuta = false },
            onConfirmar = { nuevaRuta ->
                viewModel.agregarRutaFrecuente(nuevaRuta)
                mostrarDialogoAgregarRuta = false
            }
        )
    }

    if (rutaSeleccionadaOpciones != null) {
        val ruta = rutaSeleccionadaOpciones!!
        DialogoOpcionesRutaFrecuente(
            ruta = ruta,
            onDismiss = { rutaSeleccionadaOpciones = null },
            onVerEnMapa = {
                navController.navigate("principal")
            },
            onEliminar = {
                viewModel.eliminarRutaFrecuente(ruta.id)
                Toast.makeText(context, "Ruta '${ruta.nombre}' eliminada", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (mostrarDialogoTema) {
        DialogoTema(
            temaActual = viewModel.modoTema.value,
            onDismiss = { mostrarDialogoTema = false },
            onSeleccionarTema = { nuevoTema ->
                viewModel.cambiarTema(nuevoTema)
                mostrarDialogoTema = false
            }
        )
    }

    if (mostrarDialogoNombre) {
        DialogoEditarNombre(
            nombreActual = usuarioActual,
            onDismiss = { mostrarDialogoNombre = false },
            onConfirmar = { nuevoNombre ->
                viewModel.actualizarNombreUsuario(userDao, nuevoNombre) { _ ->
                    mostrarDialogoNombre = false
                }
            }
        )
    }

    if (mostrarDialogoFoto) {
        DialogoCambiarFotoPerfil(
            fotoActualUri = viewModel.fotoPerfilUri.value,
            onDismiss = { mostrarDialogoFoto = false },
            onSeleccionarUri = { nuevaUri ->
                viewModel.actualizarFotoPerfil(nuevaUri)
                mostrarDialogoFoto = false
            }
        )
    }
}

@Composable
fun DialogoTema(
    temaActual: String,
    onDismiss: () -> Unit,
    onSeleccionarTema: (String) -> Unit
) {
    val opciones = listOf("Degradados", "Claro", "Oscuro")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleccionar Tema de la APP",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                opciones.forEach { opcion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .semantics(mergeDescendants = true) {
                                role = Role.RadioButton
                                contentDescription = if (opcion == "Degradados") "Tema Degradados por defecto" else "Tema $opcion"
                            }
                            .clickable { onSeleccionarTema(opcion) }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (opcion == temaActual),
                            onClick = { onSeleccionarTema(opcion) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (opcion == "Degradados") "Degradados (por defecto)" else opcion,
                            fontSize = 15.sp,
                            fontWeight = if (opcion == temaActual) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun DialogoEditarNombre(
    nombreActual: String,
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit
) {
    var nuevoNombre by remember { mutableStateOf(nombreActual) }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Nombre de Usuario",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Column {
                Text(
                    text = "Escribe tu nuevo nombre para actualizar la base de datos y la app:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = nuevoNombre,
                    onValueChange = {
                        nuevoNombre = it
                        errorMsg = ""
                    },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Campo de nuevo nombre de usuario" }
                )
                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nuevoNombre.isNotBlank()) {
                        onConfirmar(nuevoNombre.trim())
                    } else {
                        errorMsg = "El nombre no puede estar vacío"
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DialogoCambiarFotoPerfil(
    fotoActualUri: String?,
    onDismiss: () -> Unit,
    onSeleccionarUri: (String?) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSeleccionarUri(uri.toString())
            onDismiss()
        }
    }

    val presetsAvatares = listOf(
        Pair("autobus", "Autobús"),
        Pair("express", "Express"),
        Pair("conductor", "Conductor"),
        Pair("vip", "Pasajero VIP")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Foto de Perfil",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Elige una foto de tu galería o selecciona un avatar:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elegir foto de la Galería")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Avatares predeterminados:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    presetsAvatares.forEach { (presetKey, nombrePreset) ->
                        val (icon, bgColor) = when (presetKey) {
                            "autobus" -> Pair(Icons.Default.DirectionsBus, Color(0xFF3498DB))
                            "express" -> Pair(Icons.Default.DirectionsTransit, Color(0xFF9B59B6))
                            "conductor" -> Pair(Icons.Default.Badge, Color(0xFF2ECC71))
                            "vip" -> Pair(Icons.Default.Stars, Color(0xFFF39C12))
                            else -> Pair(Icons.Default.Person, MaterialTheme.colorScheme.primary)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Avatar $nombrePreset"
                                }
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSeleccionarUri("preset:$presetKey")
                                    onDismiss()
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(nombrePreset, fontSize = 10.sp)
                        }
                    }
                }

                if (fotoActualUri != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSeleccionarUri(null)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC0392B))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar foto actual")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
