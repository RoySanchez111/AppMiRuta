package com.example.appbanco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavegacionMiRuta()
                }
            }
        }
    }
}


fun formatCurrency(amount: Double): String {
    val locale = Locale.forLanguageTag("es-MX")
    return try {
        NumberFormat.getCurrencyInstance(locale).format(amount)
    } catch (_: Exception) {
        "$${String.format(locale, "%.2f", amount)}"
    }
}

@Composable
fun NavegacionMiRuta() {
    val navController = rememberNavController()

    var saldo by rememberSaveable { mutableDoubleStateOf(15450.00) }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { PantallaLogin(navController) }
        composable("principal") { PantallaPrincipal(navController, saldo) }
        composable("transferencia") {
            PantallaTransferencia(navController, saldo) { nuevoMonto ->
                saldo -= nuevoMonto
            } 
        }
        composable("confirmacion") { PantallaConfirmacion(navController) }
    }
}

@Composable
fun PantallaLogin(navController: NavController) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var iniciarAnimacion by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (iniciarAnimacion) 1f else 0f,
        animationSpec = tween(durationMillis = 1500), label = "alphaAnim"
    )

    LaunchedEffect(Unit) { iniciarAnimacion = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Mi Banco", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                navController.navigate("principal") {
                    popUpTo("login") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Iniciar Sesión", fontSize = 18.sp)
        }
    }
}

@Composable
fun PantallaPrincipal(navController: NavController, saldo: Double) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bienvenido de nuevo", fontSize = 18.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Saldo Disponible", fontSize = 14.sp)
                Text(formatCurrency(saldo), fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("transferencia") },
            modifier = Modifier.fillMaxWidth().height(55.dp)
        ) {
            Text("Hacer una Transferencia")
        }
    }
}

@Composable
fun PantallaTransferencia(navController: NavController, saldoActual: Double, onTransferir: (Double) -> Unit) {
    var cuenta by remember { mutableStateOf("") }
    var cantidadStr by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nueva Transferencia", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Saldo actual: ${formatCurrency(saldoActual)}", color = MaterialTheme.colorScheme.secondary)
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = cuenta,
            onValueChange = { 
                cuenta = it
                if (it.isNotEmpty()) errorMensaje = null
            },
            label = { Text("Cuenta Destino (CLABE/Tarjeta)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMensaje != null && cuenta.isEmpty()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = cantidadStr,
            onValueChange = { 
                cantidadStr = it 
                errorMensaje = null
            },
            label = { Text("Monto a enviar ($)") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMensaje != null && (cantidadStr.isEmpty() || errorMensaje?.contains("fondos") == true || errorMensaje?.contains("Monto") == true),
            trailingIcon = {
                if (errorMensaje != null) Icon(Icons.Default.Error, "Error", tint = MaterialTheme.colorScheme.error)
            }
        )

        if (errorMensaje != null) {
            Text(
                text = errorMensaje!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {

                try {
                    if (cuenta.isEmpty()) throw Exception("Ingresa una cuenta destino")
                    
                    val monto = cantidadStr.toDoubleOrNull() ?: throw Exception("Monto inválido: ingresa un número")
                    
                    if (monto <= 0) throw Exception("El monto debe ser mayor a 0")
                    if (monto > saldoActual) throw Exception("Fondos insuficientes")


                    onTransferir(monto)
                    navController.navigate("confirmacion")
                } catch (e: Exception) {
                    errorMensaje = e.message
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Transferir ahora", fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Cancelar")
        }
    }
}

@Composable
fun PantallaConfirmacion(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Éxito",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text("¡Transferencia Exitosa!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("El dinero ha sido enviado correctamente.", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                navController.navigate("principal") {
                    popUpTo("principal") { inclusive = false }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Volver a Inicio")
        }
    }
}
