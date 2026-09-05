package com.example.appbanco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.appbanco.data.database.UserDao
import com.example.appbanco.data.database.UserEntity
import com.example.appbanco.logic.SecurityUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PantallaRegistro(navController: NavController, userDao: UserDao? = null) {

    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }

    var mensajeError by remember { mutableStateOf("") }
    var datosCorrectos by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear cuenta",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                mensajeError = ""
                datosCorrectos = false
            },
            label = {
                Text("Correo electrónico")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
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
                if (
                    nuevoValor.all { it.isDigit() } &&
                    nuevoValor.length <= 10
                ) {
                    telefono = nuevoValor
                }

                mensajeError = ""
                datosCorrectos = false
            },
            label = {
                Text("Teléfono")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
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
            label = {
                Text("Contraseña")
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
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
            label = {
                Text("Confirmar contraseña")
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
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
                if (
                    nuevoValor.all { it.isDigit() } &&
                    nuevoValor.length <= 3
                ) {
                    edad = nuevoValor
                }

                mensajeError = ""
                datosCorrectos = false
            },
            label = {
                Text("Edad")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Campo de edad" },
            colors = coloresRegistro()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = Color(0xFFFFCDD2),
                fontSize = 14.sp,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (datosCorrectos) {
            Text(
                text = "Cuenta creada exitosamente. Redirigiendo...",
                color = Color(0xFFB9F6CA),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                val correoLimpio = correo.trim()
                val passwordLimpio = password.trim()
                val confirmarPasswordLimpio = confirmarPassword.trim()
                val telefonoLimpio = telefono.trim()
                val edadLimpia = edad.trim()

                mensajeError = validarRegistro(
                    correo = correoLimpio,
                    telefono = telefonoLimpio,
                    password = passwordLimpio,
                    confirmarPassword = confirmarPasswordLimpio,
                    edad = edadLimpia
                )

                if (mensajeError.isEmpty()) {
                    scope.launch {
                        // Verificar si el usuario ya existe en la BD
                        val usuarioExistente = userDao?.getUserByUsername(correoLimpio)
                        if (usuarioExistente != null) {
                            mensajeError = "El correo/usuario ya se encuentra registrado"
                            datosCorrectos = false
                        } else {
                            datosCorrectos = true
                            // Guardar credenciales en la Base de Datos Room
                            userDao?.registerUser(
                                UserEntity(
                                    username = correoLimpio,
                                    passwordHash = SecurityUtils.hashPassword(passwordLimpio)
                                )
                            )
                            delay(1000)
                            navController.popBackStack()
                        }
                    }
                } else {
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
                fontWeight = FontWeight.Bold
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
                color = Color.White
            )
        }
    }
}

fun validarRegistro(
    correo: String,
    telefono: String,
    password: String,
    confirmarPassword: String,
    edad: String
): String {

    if (
        correo.isBlank() ||
        telefono.isBlank() ||
        password.isBlank() ||
        confirmarPassword.isBlank() ||
        edad.isBlank()
    ) {
        return "Completa todos los campos"
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

    if (edadNumero == null || edadNumero !in 1..120) {
        return "Ingresa una edad válida"
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
