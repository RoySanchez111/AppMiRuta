package com.example.appbanco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
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
import com.example.appbanco.ui.components.TimeBasedBackground
import java.util.Calendar
import java.text.NumberFormat
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashTheme = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> R.style.Theme_MiBanco_Splash_Madrugada
            in 6..11 -> R.style.Theme_MiBanco_Splash_Manana
            in 12..17 -> R.style.Theme_MiBanco_Splash_Atardecer
            in 18..19 -> R.style.Theme_MiBanco_Splash_Ocaso
            else -> R.style.Theme_MiBanco_Splash_Noche
        }
        setTheme(splashTheme)
        
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TimeBasedBackground {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        NavegacionMiRuta()
                    }
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

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash"){PantallaSplash(navController)}
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
fun PantallaSplash(navController: NavController){
    LaunchedEffect(Unit) {
        delay(2000.milliseconds)
        navController.navigate("login"){
            popUpTo("splash"){
                inclusive = true
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = painterResource(id = R.drawable.logo_miruta),
            contentDescription = "Logo MiRuta",
            modifier = Modifier.size(240.dp)
        )
    }
}

@Composable
fun PantallaLogin(navController: NavController) {
    //Colección
    val usuarios = mapOf(
        "roy" to "123"
    )
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeError by remember {mutableStateOf("")} //Variable de mensaje de error
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
        Text(
            "Mi Ruta", 
            fontSize = 36.sp, 
            fontWeight = FontWeight.Bold, 
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))

        //Muestra el mensaje de error
        if (mensajeError.isNotEmpty()){
            Text(
                text = mensajeError,
                color = Color.White
            )
        }
        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = {
                Text(
                    "Usuario", color = Color.Black,
                    modifier = Modifier.background(Color.White))//Fondo Blanco para la etiqueta)
                },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                //Fondo del recuadro
                unfocusedContainerColor =Color.White,
                focusedContainerColor = Color.White,

                //Color del texto
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,

                //Color del borde
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,

                //Color del cursor
                cursorColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(
                "Contraseña", color = Color.Black,//Color del texto Contraseña
                modifier = Modifier.background(Color.White))//Fondo Blanco para la etiqueta
                    },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor =Color.White,

                //Color del texto
                unfocusedTextColor = Color.Black,
                focusedTextColor = Color.Black,

                //Color del borde
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,

                //Color del cursor
                cursorColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (usuarios[usuario]== password) {
                    navController.navigate("principal") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    mensajeError = "Usuario  o contraseña incorrecto"
                }
        },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Iniciar Sesión", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))

        //Botón de iniciar sesión con Google
        Button(
            onClick = {

            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black)
        )
        {
            Text("Iniciar sesión con Google")
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "O",
            color = Color.White,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        //Boton de iniciar sesión como invitado
        Button(
            onClick = {
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray,
                contentColor = Color.White
            )
        ) {
            Text("Continuar como invitado")
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
        Text("Bienvenido de nuevo", fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f))
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
        Text("Nueva Transferencia", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Saldo actual: ${formatCurrency(saldoActual)}", color = Color.White.copy(alpha = 0.9f))
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = cuenta,
            onValueChange = { 
                cuenta = it
                if (it.isNotEmpty()) errorMensaje = null
            },
            label = { Text("Cuenta Destino (CLABE/Tarjeta)", color = Color.White) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMensaje != null && cuenta.isEmpty(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                focusedBorderColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                focusedLabelColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = cantidadStr,
            onValueChange = { 
                cantidadStr = it 
                errorMensaje = null
            },
            label = { Text("Monto a enviar ($)", color = Color.White) },
            placeholder = { Text("0.00", color = Color.White.copy(alpha = 0.5f)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMensaje != null && (cantidadStr.isEmpty() || errorMensaje?.contains("fondos") == true || errorMensaje?.contains("Monto") == true),
            trailingIcon = {
                if (errorMensaje != null) Icon(Icons.Default.Error, "Error", tint = MaterialTheme.colorScheme.error)
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                focusedBorderColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                focusedLabelColor = Color.White
            )
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

        Text("¡Transferencia Exitosa!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("El dinero ha sido enviado correctamente.", color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(top = 8.dp))

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
