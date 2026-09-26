package com.example.appbanco.ui.screens

import android.util.Log
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.School
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.appbanco.R
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavController
import com.example.appbanco.ui.viewmodel.LoginViewModel

import com.example.appbanco.logic.SessionManager
import com.example.appbanco.data.database.AppDatabase
import com.example.appbanco.data.database.SyncManager
import com.example.appbanco.data.database.UserEntity
import com.example.appbanco.logic.SecurityUtils
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun PantallaLogin(
    navController: NavController,
    viewModel: LoginViewModel,
    sessionManager: SessionManager
) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var mostrarDialogoGoogle by remember { mutableStateOf(false) }
    
    val loginSuccess = viewModel.loginSuccess
    val errorMessage = viewModel.errorMessage
    
    var iniciarAnimacion by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (iniciarAnimacion) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "alphaAnim"
    )

    LaunchedEffect(Unit) { iniciarAnimacion = true }

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            val role = viewModel.userRole
            val destinoFinal = if (role == "conductor" || role == "admin") {
                "inicio_conductor"
            } else {
                val tutorialCompletado = sessionManager.hasCompletedTutorial.firstOrNull() ?: false
                if (!tutorialCompletado) "tutorial" else "principal"
            }
            navController.navigate(destinoFinal) {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    val configuration = LocalConfiguration.current
    val esTablet = configuration.screenWidthDp >= 600

    if (esTablet) {
        // 🖥️ DISEÑO ADAPTABLE DE DOS COLUMNAS PARA TABLETS (FRAME 7)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
                .alpha(alpha),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // COLUMNA IZQUIERDA: Marca / Logo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_blanco_vector),
                    contentDescription = null,
                    modifier = Modifier.size(140.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.ic_miruta_texto),
                    contentDescription = "Mi Ruta Logo",
                    modifier = Modifier
                        .width(240.dp)
                        .height(64.dp)
                        .semantics { heading() }
                )
            }

            // COLUMNA DERECHA: Tarjeta de Formulario de Login
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 480.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage, 
                            color = Color(0xFFFFD2D2),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    OutlinedTextField(
                        value = usuario,
                        onValueChange = { usuario = it },
                        label = { Text("Usuario") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Campo para ingresar usuario" },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = description, tint = MaterialTheme.colorScheme.onSurface)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { viewModel.onLoginClick(usuario, password) }),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Campo para ingresar contraseña" },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    Button(
                        onClick = { viewModel.onLoginClick(usuario, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .semantics { role = Role.Button; contentDescription = "Boton iniciar sesión" },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEB30F), contentColor = Color.White)
                    ) {
                        Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { navController.navigate("registro") },
                        modifier = Modifier.semantics { role = Role.Button; contentDescription = "Crear nueva cuenta" }
                    ) {
                        Text(text = "¿No tienes cuenta? Crear cuenta", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            mostrarDialogoGoogle = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(4.dp, RoundedCornerShape(25.dp))
                            .semantics { role = Role.Button; contentDescription = "Iniciar sesión con Google" },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF3C4043)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(text = "G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Iniciar sesión con Google", color = Color(0xFF3C4043), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "O", color = Color.White, fontSize = 16.sp, modifier = Modifier.semantics { contentDescription = "O" })
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                sessionManager.saveSession(0, "Invitado", "token_invitado")
                                val tutorialCompletado = sessionManager.hasCompletedTutorial.firstOrNull() ?: false
                                val destinoFinal = if (!tutorialCompletado) "tutorial" else "principal"
                                navController.navigate(destinoFinal) {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .semantics { role = Role.Button; contentDescription = "Boton continuar como invitado" },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White)
                    ) {
                        Text("Continuar como invitado", fontSize = 14.sp)
                    }
                }
            }
        }
    } else {
        // 📱 DISEÑO VERTICAL TRADICIONAL PARA TELÉFONOS
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_blanco_vector),
                contentDescription = null,
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_miruta_texto),
                contentDescription = "Mi Ruta Logo Texto",
                modifier = Modifier
                    .width(180.dp)
                    .height(48.dp)
                    .semantics { heading() }
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage, 
                    color = Color(0xFFFFD2D2),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it },
                label = { Text("Usuario") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Campo para ingresar usuario" },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.onLoginClick(usuario, password) }),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Campo para ingresar contraseña" },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { viewModel.onLoginClick(usuario, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .semantics { role = Role.Button; contentDescription = "Boton iniciar sesión" },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEB30F), contentColor = Color.White)
            ) {
                Text("Iniciar Sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { navController.navigate("registro") },
                modifier = Modifier.semantics { role = Role.Button; contentDescription = "Crear nueva cuenta" }
            ) {
                Text(text = "¿No tienes cuenta? Crear cuenta", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    mostrarDialogoGoogle = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(4.dp, RoundedCornerShape(25.dp))
                    .semantics { role = Role.Button; contentDescription = "Iniciar sesión con Google" },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF3C4043)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = "G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Iniciar sesión con Google", color = Color(0xFF3C4043), fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "O", color = Color.White, fontSize = 18.sp, modifier = Modifier.semantics { contentDescription = "O" })
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        sessionManager.saveSession(0, "Invitado", "token_invitado")
                        val tutorialCompletado = sessionManager.hasCompletedTutorial.firstOrNull() ?: false
                        val destinoFinal = if (!tutorialCompletado) "tutorial" else "principal"
                        navController.navigate(destinoFinal) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .semantics { role = Role.Button; contentDescription = "Boton continuar como invitado" },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White)
            ) {
                Text("Continuar como invitado")
            }
        }
    }

    if (mostrarDialogoGoogle) {
        DialogoGoogleSignIn(
            onDismiss = { mostrarDialogoGoogle = false },
            onSuccess = { email ->
                mostrarDialogoGoogle = false
                scope.launch {
                    val usernameFromEmail = email.substringBefore("@")
                    val newUser = UserEntity(
                        username = usernameFromEmail,
                        passwordHash = SecurityUtils.hashPassword("google_oauth_$email")
                    )
                    try {
                        val userDao = AppDatabase.getDatabase(context).userDao()
                        userDao.registerUser(newUser)
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Error al registrar usuario en la BD local", e)
                    }
                    try {
                        val syncManager = SyncManager()
                        syncManager.syncUserToCloud(newUser)
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Error al sincronizar usuario con la nube", e)
                    }

                    sessionManager.saveSession(99, usernameFromEmail, "token_google_$email")
                    val tutorialCompletado = sessionManager.hasCompletedTutorial.firstOrNull() ?: false
                    val destinoFinal = if (!tutorialCompletado) "tutorial" else "principal"
                    delay(800)
                    navController.navigate(destinoFinal) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        )
    }
}

@Composable
fun DialogoGoogleSignIn(
    onDismiss: () -> Unit,
    onSuccess: (email: String) -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Iniciar sesión", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF202124))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Usa tu cuenta de Google", fontSize = 14.sp, color = Color(0xFF5F6368))
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = correo,
                    onValueChange = { 
                        correo = it
                        errorMsg = ""
                    },
                    label = { Text("Correo electrónico o teléfono") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorMsg = ""
                    },
                    label = { Text("Contraseña de Google") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                if (cargando) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val emailTrim = correo.trim()
                    val passTrim = password.trim()
                    if (emailTrim.isBlank() || !emailTrim.contains("@")) {
                        errorMsg = "Introduce un correo electrónico válido"
                    } else if (passTrim.isBlank()) {
                        errorMsg = "Introduce tu contraseña"
                    } else if (passTrim.length < 8) {
                        errorMsg = "La contraseña debe tener mínimo 8 caracteres"
                    } else if (!passTrim.any { it.isLetter() } || !passTrim.any { it.isDigit() }) {
                        errorMsg = "La contraseña debe contener letras y números"
                    } else {
                        cargando = true
                        scope.launch {
                            delay(1000)
                            onSuccess(emailTrim)
                        }
                    }
                },
                enabled = !cargando
            ) {
                Text("Siguiente")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !cargando
            ) {
                Text("Cancelar", color = Color(0xFF1A73E8))
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}
