package com.example.appbanco.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appbanco.logic.AppConfig

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.appbanco.ui.viewmodel.LoginViewModel

@Composable
fun PantallaLogin(navController: NavController, viewModel: LoginViewModel) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
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
            navController.navigate("principal") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Mi Ruta", 
            fontSize = 36.sp, 
            fontWeight = FontWeight.Bold, 
            color = Color.White,
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        if (errorMessage != null) {
            Text(text = errorMessage, color = Color.White)
        }
        
        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = {
                Text("Usuario", color = Color.Black, modifier = Modifier.background(Color.White))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                cursorColor = Color.Black
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text("Contraseña", color = Color.Black, modifier = Modifier.background(Color.White))
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,
                cursorColor = Color.Black
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                viewModel.onLoginClick(usuario, password)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFEB30F),
                contentColor = Color.White
            )
        ) {
            Text("Iniciar Sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.15f), 
                contentColor = Color.White
            )
        ) {
            Text("Iniciar sesión con Google")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "O", color = Color.White, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.1f), 
                contentColor = Color.White
            )
        ) {
            Text("Continuar como invitado")
        }
    }
}
