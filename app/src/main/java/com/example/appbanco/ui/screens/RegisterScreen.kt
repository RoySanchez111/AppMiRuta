package com.example.appbanco.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appbanco.R
import com.example.appbanco.data.database.UserDao
import com.example.appbanco.data.database.UserEntity
import com.example.appbanco.logic.SecurityUtils
import com.example.appbanco.logic.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun PantallaRegistro(
    navController: NavController,
    userDao: UserDao? = null,
    sessionManager: SessionManager? = null
) {
    var usuarioInput by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }

    var mensajeError by remember { mutableStateOf("") }
    var datosCorrectos by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.logo_blanco),
                contentDescription = null,
                modifier = Modifier.size(46.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_miruta_texto),
                contentDescription = "Mi Ruta Logo",
                modifier = Modifier
                    .width(130.dp)
                    .height(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Crear cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // TARJETA CONTENEDORA DE FORMULARIO CON BORDE Y DIFUMINADO
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                OutlinedTextField(
                    value = usuarioInput,
                    onValueChange = {
                        usuarioInput = it
                        mensajeError = ""
                        datosCorrectos = false
                    },
                    label = { Text("Nombre de usuario") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Campo para nombre de usuario" },
                    colors = coloresRegistro()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        mensajeError = ""
                        datosCorrectos = false
                    },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Black) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Campo de correo electrónico" },
                    colors = coloresRegistro()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { nuevoValor ->
                        if (nuevoValor.all { it.isDigit() } && nuevoValor.length <= 10) {
                            telefono = nuevoValor
                        }
                        mensajeError = ""
                        datosCorrectos = false
                    },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Black) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Campo de teléfono de 10 dígitos" },
                    colors = coloresRegistro()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        mensajeError = ""
                        datosCorrectos = false
                    },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Campo de contraseña" },
                    colors = coloresRegistro()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmarPassword,
                    onValueChange = {
                        confirmarPassword = it
                        mensajeError = ""
                        datosCorrectos = false
                    },
                    label = { Text("Confirmar contraseña") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color.Black) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Campo para confirmar contraseña" },
                    colors = coloresRegistro()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = edad,
                    onValueChange = { nuevoValor ->
                        if (nuevoValor.all { it.isDigit() } && nuevoValor.length <= 3) {
                            edad = nuevoValor
                        }
                        mensajeError = ""
                        datosCorrectos = false
                    },
                    label = { Text("Edad (13 a 98 años)") },
                    leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = Color.Black) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Campo de edad" },
                    colors = coloresRegistro()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mensajeError.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFCDD2),
                border = BorderStroke(1.dp, Color(0xFFC0392B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠️ $mensajeError",
                    color = Color(0xFFC0392B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(12.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (datosCorrectos) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9),
                border = BorderStroke(1.dp, Color(0xFF2ECC71)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ ¡Cuenta creada exitosamente! Redirigiendo...",
                    color = Color(0xFF27AE60),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(12.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                val usuarioLimpio = usuarioInput.trim()
                val correoLimpio = correo.trim()
                val passwordLimpio = password.trim()
                val confirmarPasswordLimpio = confirmarPassword.trim()
                val telefonoLimpio = telefono.trim()
                val edadLimpia = edad.trim()

                mensajeError = validarRegistro(
                    usuario = usuarioLimpio,
                    correo = correoLimpio,
                    telefono = telefonoLimpio,
                    password = passwordLimpio,
                    confirmarPassword = confirmarPasswordLimpio,
                    edad = edadLimpia
                )

                if (mensajeError.isEmpty()) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        val usuarioExistente = userDao?.getUserByUsername(usuarioLimpio)
                        if (usuarioExistente != null) {
                            mensajeError = "El nombre de usuario ya se encuentra registrado"
                            datosCorrectos = false
                        } else {
                            datosCorrectos = true
                            userDao?.registerUser(
                                UserEntity(
                                    username = usuarioLimpio,
                                    passwordHash = SecurityUtils.hashPassword(passwordLimpio)
                                )
                            )
                            if (sessionManager != null) {
                                sessionManager.saveSession(1, usuarioLimpio, "token_registro")
                                val tutorialCompletado = sessionManager.hasCompletedTutorial.firstOrNull() ?: false
                                val destinoFinal = if (!tutorialCompletado) "tutorial" else "principal"
                                delay(500)
                                navController.navigate(destinoFinal) {
                                    popUpTo("registro") { inclusive = true }
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                delay(1000)
                                navController.popBackStack()
                            }
                        }
                    }
                } else {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    datosCorrectos = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Boton registrar cuenta"
                },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB313),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Crear cuenta",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier.semantics {
                role = Role.Button
                contentDescription = "Volver al inicio de sesión"
            }
        ) {
            Text(
                text = "Ya tengo una cuenta",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun validarRegistro(
    usuario: String,
    correo: String,
    telefono: String,
    password: String,
    confirmarPassword: String,
    edad: String
): String {

    if (
        usuario.isBlank() ||
        correo.isBlank() ||
        telefono.isBlank() ||
        password.isBlank() ||
        confirmarPassword.isBlank() ||
        edad.isBlank()
    ) {
        return "Completa todos los campos obligatorios"
    }

    if (usuario.length < 3) {
        return "El nombre de usuario debe tener mínimo 3 caracteres"
    }

    val formatoCorreo =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    if (!formatoCorreo.matches(correo)) {
        return "Escribe un correo electrónico válido"
    }

    if (telefono.length != 10) {
        return "El teléfono debe contener 10 números"
    }

    if (password.length < 8) {
        return "La contraseña debe tener mínimo 8 caracteres"
    }

    if (
        !password.any { it.isLetter() } ||
        !password.any { it.isDigit() }
    ) {
        return "La contraseña debe contener letras y números"
    }

    if (password != confirmarPassword) {
        return "Las contraseñas no coinciden"
    }

    val edadNumero = edad.toIntOrNull()

    if (edadNumero == null || edadNumero <= 12 || edadNumero >= 99) {
        return "Debes ser mayor de 12 años y menor de 99 años para registrarte"
    }

    return ""
}

@Composable
fun coloresRegistro() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = Color.White,
    focusedContainerColor = Color.White,
    unfocusedTextColor = Color.Black,
    focusedTextColor = Color.Black,
    unfocusedLabelColor = Color.Black,
    focusedLabelColor = Color.Black,
    cursorColor = Color.Black
)
