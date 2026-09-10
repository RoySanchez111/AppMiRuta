package com.example.appbanco

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appbanco.data.database.AppDatabase
import com.example.appbanco.data.database.UserEntity
import com.example.appbanco.logic.SecurityUtils
import com.example.appbanco.logic.SessionManager
import com.example.appbanco.logic.obtenerEsquemaColoresClaro
import com.example.appbanco.logic.obtenerEsquemaColoresDinamico
import com.example.appbanco.logic.obtenerEsquemaColoresOscuro
import com.example.appbanco.ui.navigation.NavegacionMiRuta
import com.example.appbanco.ui.theme.obtenerTipografiaPersonalizada
import com.example.appbanco.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var database: AppDatabase

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

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
        setTheme(R.style.Theme_MiRuta)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        solicitarPermisosIniciales()
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
            MaterialTheme(
                colorScheme = colorScheme,
                typography = obtenerTipografiaPersonalizada()
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

    private fun solicitarPermisosIniciales() {
        val permisos = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisos.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val faltantes = permisos.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (faltantes.isNotEmpty()) {
            requestPermissionLauncher.launch(faltantes.toTypedArray())
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
