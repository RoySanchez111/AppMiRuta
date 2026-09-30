package com.example.appbanco

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.appbanco.ui.navigation.NavegacionMiRuta
import com.example.appbanco.ui.theme.obtenerTipografiaPersonalizada
import java.util.Calendar

import com.example.appbanco.logic.obtenerEsquemaColoresDinamico
import com.example.appbanco.logic.obtenerEsquemaColoresClaro
import com.example.appbanco.logic.obtenerEsquemaColoresOscuro
import com.example.appbanco.logic.SessionManager
import com.example.appbanco.ui.viewmodel.MainViewModel
import com.example.appbanco.data.database.AppDatabase
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.appbanco.data.database.SyncManager
import com.example.appbanco.data.database.UserEntity
import com.example.appbanco.logic.SecurityUtils
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        sessionManager = SessionManager(this)
        database = AppDatabase.getDatabase(this)
        val splashTheme = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> R.style.Theme_MiRuta_Splash_Madrugada
            in 6..11 -> R.style.Theme_MiRuta_Splash_Manana
            in 12..17 -> R.style.Theme_MiRuta_Splash_Atardecer
            in 18..19 -> R.style.Theme_MiRuta_Splash_Ocaso
            else -> R.style.Theme_MiRuta_Splash_Noche
        }
        setTheme(splashTheme)

        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 🛡️ PRE-POBLACIÓN SEGURA Y AUTOSINCRONIZACIÓN A FIRESTORE
        lifecycleScope.launch {
            val userDao = database.userDao()
            val sync = SyncManager()

            val conductor = userDao.getUserByUsername("rafa") ?: UserEntity(username = "rafa", passwordHash = SecurityUtils.hashPassword("123"), role = "conductor").also { userDao.registerUser(it) }
            val admin = userDao.getUserByUsername("roy") ?: UserEntity(username = "roy", passwordHash = SecurityUtils.hashPassword("123"), role = "admin").also { userDao.registerUser(it) }
            val pasajero = userDao.getUserByUsername("alex") ?: UserEntity(username = "alex", passwordHash = SecurityUtils.hashPassword("123"), role = "pasajero").also { userDao.registerUser(it) }

            val uRafa = userDao.getUserByUsername("rafa") ?: conductor
            val uRoy = userDao.getUserByUsername("roy") ?: admin
            val uAlex = userDao.getUserByUsername("alex") ?: pasajero

            // Sincronizar usuarios por defecto hacia Firestore
            sync.syncUserToCloud(uRafa)
            sync.syncUserToCloud(uRoy)
            sync.syncUserToCloud(uAlex)

            // Posición inicial del conductor de prueba en Firestore
            sync.broadcastConductorLocation(
                conductorId = "rafa",
                nombre = "Rafael",
                ruta = "Línea L1",
                lat = 18.999446,
                lng = -98.261833,
                activo = true
            )

            // Incidencia inicial de prueba en Firestore
            sync.syncIncidenciaToCloud(
                id = "inc-101",
                tipo = "Tráfico",
                ruta = "L1",
                titulo = "Tráfico fluido en Blvd. Atlixco",
                descripcion = "Unidades operando con normalidad.",
                tiempo = "Hace 5 min",
                confirmaciones = 2,
                esGlobal = true
            )
        }

        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return MainViewModel(sessionManager) as T
                        }
                        throw IllegalArgumentException("Clase ViewModel no conocida: ${modelClass.name}")
                    }
                }
            )

            val modoTema by mainViewModel.modoTema.collectAsState()
            val escalaFuente by mainViewModel.escalaFuente.collectAsState()
            
            val colorScheme = when (modoTema) {
                "Claro" -> obtenerEsquemaColoresClaro()
                "Oscuro" -> obtenerEsquemaColoresOscuro()
                else -> obtenerEsquemaColoresDinamico()
            }

            val fontScaleFactor = escalaFuente.factor
            val currentDensity = LocalDensity.current

            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = currentDensity.density,
                    fontScale = currentDensity.fontScale * fontScaleFactor
                )
            ) {
                MaterialTheme(
                    colorScheme = colorScheme,
                    typography = obtenerTipografiaPersonalizada(escalaFuente)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = colorScheme.background
                    ) {
                        NavegacionMiRuta(mainViewModel, database, sessionManager)
                    }
                }
            }
        }
    }
}
