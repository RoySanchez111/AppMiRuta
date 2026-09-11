package com.example.appbanco

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
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
        
        prePoblarBaseDeDatos()

        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MainViewModel(sessionManager) as T
                    }
                }
            )
            
            val colorScheme = when (mainViewModel.modoTema.value) {
                "Claro" -> obtenerEsquemaColoresClaro()
                "Oscuro" -> obtenerEsquemaColoresOscuro()
                else -> obtenerEsquemaColoresDinamico()
            }

            val esFuenteGrande = mainViewModel.fuenteGrande.value
            val fontScaleFactor = if (esFuenteGrande) 1.35f else 1.15f
            val currentDensity = LocalDensity.current

            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = currentDensity.density,
                    fontScale = currentDensity.fontScale * fontScaleFactor
                )
            ) {
                MaterialTheme(
                    colorScheme = colorScheme,
                    typography = obtenerTipografiaPersonalizada(esFuenteGrande)
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

    private fun prePoblarBaseDeDatos() {

        lifecycleScope.launch {
            val userDao = database.userDao()
            if (userDao.getUserByUsername("roy") == null) {
                userDao.registerUser(UserEntity(username = "roy", passwordHash = SecurityUtils.hashPassword("123")))
            }
            if (userDao.getUserByUsername("alex") == null) {
                userDao.registerUser(UserEntity(username = "alex", passwordHash = SecurityUtils.hashPassword("456")))
            }
        }
    }
}
