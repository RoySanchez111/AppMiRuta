package com.example.appbanco.ui.screens

import com.example.appbanco.logic.SessionManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appbanco.data.alertasSimuladas
import com.example.appbanco.data.lineasDeRuta
import com.example.appbanco.ui.components.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PantallaPrincipal(navController: NavController) {
    val onBackground = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // BUSCADOR
        Surface(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(18.dp),
            color = onBackground.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, onBackground.copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = onBackground.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Buscar línea o parada...", color = onBackground.copy(alpha = 0.4f), fontSize = 14.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // MAPA
        Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(Color(0xFFE7E3DB), RoundedCornerShape(20.dp))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(Color(0xFFCFCBC4), Offset(0f, size.height * 0.3f), Offset(size.width, size.height * 0.3f), 28f)
                drawLine(Color(0xFFCFCBC4), Offset(0f, size.height * 0.6f), Offset(size.width, size.height * 0.6f), 32f)
                drawLine(Color(0xFFCFCBC4), Offset(size.width * 0.25f, 0f), Offset(size.width * 0.25f, size.height), 28f)
                drawLine(Color(0xFFCFCBC4), Offset(size.width * 0.7f, 0f), Offset(size.width * 0.7f, size.height), 28f)
                drawLine(Color(0xFF4DA3E8), Offset(size.width * 0.15f, size.height * 0.67f), Offset(size.width * 0.85f, size.height * 0.67f), 12f, cap = StrokeCap.Round)
                drawLine(Color(0xFFFFA23A), Offset(size.width * 0.2f, size.height * 0.4f), Offset(size.width * 0.75f, size.height * 0.4f), 10f, cap = StrokeCap.Round)
                val paradas = listOf(Offset(size.width * 0.25f, size.height * 0.67f), Offset(size.width * 0.45f, size.height * 0.67f), Offset(size.width * 0.65f, size.height * 0.67f))
                paradas.forEach { drawCircle(Color.White, 13f, it); drawCircle(Color(0xFFE53935), 7f, it) }
                drawCircle(Color.White, 18f, Offset(size.width * 0.52f, size.height * 0.78f))
                drawCircle(Color(0xFF4285F4), 11f, Offset(size.width * 0.52f, size.height * 0.78f))
            }
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.BottomEnd).padding(14.dp).background(Color.White, CircleShape).shadow(4.dp, CircleShape)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación", tint = Color(0xFF4285F4))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHorarios(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val onBackground = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = "", 
            onValueChange = {}, 
            placeholder = { Text("Buscar línea o parada...", color = onBackground.copy(alpha = 0.5f), fontSize = 14.sp) }, 
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = onBackground.copy(alpha = 0.5f)) }, 
            modifier = Modifier.fillMaxWidth().height(52.dp).background(onBackground.copy(alpha = 0.05f), RoundedCornerShape(12.dp)), 
            shape = RoundedCornerShape(12.dp), 
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = onBackground.copy(alpha = 0.1f), 
                focusedBorderColor = onBackground.copy(alpha = 0.3f),
                cursorColor = onBackground,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedTextColor = onBackground,
                focusedTextColor = onBackground
            ), 
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = Color(0xFFFF3B30), shape = RoundedCornerShape(20.dp)) { 
                Text("Ahora - 09:24", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold) 
            }
            listOf("Mañana", "Lun 25 ago").forEach { texto -> 
                Surface(
                    color = onBackground.copy(alpha = 0.05f), 
                    border = BorderStroke(1.dp, onBackground.copy(alpha = 0.1f)), 
                    shape = RoundedCornerShape(20.dp)
                ) { 
                    Text(texto, color = onBackground.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) 
                } 
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) { 
            Icon(Icons.Default.StarBorder, null, tint = onBackground.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("TODAS LAS LÍNEAS", color = onBackground.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold) 
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        lineasDeRuta.forEach { LineCard(it, scope, snackbarHostState) }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Próximas salidas Plaza Mayor", color = onBackground.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = Color(0xFF389338), shape = RoundedCornerShape(20.dp)) { 
                Text("09:25 - ahora", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold) 
            }
            listOf("09:35", "09:45").forEach { texto -> 
                Surface(
                    color = onBackground.copy(alpha = 0.05f), 
                    border = BorderStroke(1.dp, onBackground.copy(alpha = 0.1f)), 
                    shape = RoundedCornerShape(20.dp)
                ) { 
                    Text(texto, color = onBackground.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) 
                } 
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.popBackStack() }, 
            modifier = Modifier.fillMaxWidth().height(52.dp), 
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2697B5), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) { 
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver", fontWeight = FontWeight.Bold) 
        }
    }
}

@Composable
fun PantallaAlertas(navController: NavController) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = Color(0xFF282869)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            alertasSimuladas.forEach { AlertCard(it); Spacer(modifier = Modifier.height(16.dp)) }
            Spacer(modifier = Modifier.height(100.dp))
        }
        
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .height(56.dp)
                .width(280.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp))
                .clickable { navController.navigate("reportar") }, 
            shape = RoundedCornerShape(28.dp), 
            color = surfaceColor, 
            border = BorderStroke(1.dp, onSurface.copy(alpha = 0.1f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Box(modifier = Modifier.size(32.dp).background(primaryColor, CircleShape), contentAlignment = Alignment.Center) { 
                    Icon(Icons.Default.PriorityHigh, null, tint = Color.White, modifier = Modifier.size(18.dp)) 
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Reportar Incidencia", 
                    color = onSurface, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun PantallaReportarIncidente(navController: NavController) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(surfaceColor)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(), 
            shape = RoundedCornerShape(24.dp), 
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), 
            elevation = CardDefaults.cardElevation(0.dp), 
            border = BorderStroke(1.dp, onSurface.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("¿Qué tipo de incidente deseas reportar?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onSurface, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(color = Color(0xFFC0392B), shape = RoundedCornerShape(12.dp)) { Text("Retraso grave", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold) }
                        Spacer(modifier = Modifier.height(12.dp)); Box(modifier = Modifier.size(72.dp).background(Color(0xFFFADBD8), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Warning, null, tint = Color(0xFFC0392B), modifier = Modifier.size(40.dp)) }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(color = Color(0xFFF39C12), shape = RoundedCornerShape(12.dp)) { Text("Desvío de Ruta", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold) }
                        Spacer(modifier = Modifier.height(12.dp)); Box(modifier = Modifier.size(72.dp).background(Color(0xFFFDEBD0), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.Shortcut, null, tint = Color(0xFFF39C12), modifier = Modifier.size(40.dp)) }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Text("¿En que ruta sucedió?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onSurface)
                
                Spacer(modifier = Modifier.height(20.dp))
                val rutas = listOf(Triple("L1", Color(0xFFE57373), "Plaza Mayor - Aeropuerto"), Triple("L4", Color(0xFF64B5F6), "Universidad - Puerto"), Triple("L7", Color(0xFF66BB6A), "Centro - Hospital Norte"), Triple("L5", Color(0xFFFFB74D), "Ruta Guadalupana (Centro - Las Lomas)"), Triple("MA", Color(0xFF9575CD), "Ruta Angelópolis (Angelópolis - Loreto)"))
                rutas.forEach { (tag, color, desc) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(42.dp).background(color, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text(tag, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                        Spacer(modifier = Modifier.width(16.dp)); Text(desc, fontSize = 14.sp, color = onSurface.copy(alpha = 0.7f), fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.popBackStack() }, 
            modifier = Modifier.align(Alignment.CenterHorizontally).height(52.dp).width(220.dp), 
            colors = ButtonDefaults.buttonColors(containerColor = onSurface.copy(alpha = 0.1f), contentColor = onSurface), 
            shape = RoundedCornerShape(26.dp)
        ) { 
            Text("Enviar Reporte", fontWeight = FontWeight.Bold, fontSize = 16.sp) 
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PantallaCuenta(sessionManager: SessionManager, navController: NavController) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onBackground = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    var modoOffline by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(surfaceColor)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).background(onSurface.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { 
                Text("L", fontWeight = FontWeight.Bold, color = onSurface, fontSize = 24.sp) 
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column { 
                Text("Leo Sanchez", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = onSurface)
                Text("leo.sanchez.lo@gmail.com", fontSize = 13.sp, color = onSurface.copy(alpha = 0.6f)) 
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Text("Rutas frecuentes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF4A86F7)) { 
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) { 
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Home, "Casa", tint = Color.White, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Casa", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        Text("Registrar ubicación", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp) 
                    } 
                }
                Surface(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFFF26E68)) { 
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) { 
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.School, "Universidad", tint = Color.White, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Universidad", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        Text("Registrar ubicación", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp) 
                    } 
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Text("Modo Offline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = onSurface.copy(alpha = 0.05f)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Habilitar descarga de mapas", fontSize = 13.sp, color = onSurface.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
                    Switch(checked = modoOffline, onCheckedChange = { modoOffline = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFEB30F)))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), shape = RoundedCornerShape(24.dp), color = onBackground.copy(alpha = 0.03f)) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OpcionCuenta("Configuración"); OpcionCuenta("Alertas"); OpcionCuenta("Privacidad y Seguridad"); OpcionCuenta("Ayuda y Soporte")
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
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}
