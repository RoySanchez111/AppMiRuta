package com.example.appbanco.ui.screens

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appbanco.R
import com.example.appbanco.ui.components.MinimalistAnimation
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest

import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

@Composable
fun PantallaSplash(navController: NavController) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    LaunchedEffect(Unit) {
        delay(2500.milliseconds)
        navController.navigate("loading") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Pantalla de bienvenida, cargando animación MiRuta"
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(R.drawable.logo_gif_miruta)
                    .build(),
                imageLoader = imageLoader
            ),
            contentDescription = "Logotipo animado de MiRuta",
            modifier = Modifier.size(240.dp)
        )
    }
}

@Composable
fun PantallaLoading(navController: NavController, targetDestination: String) {
    LaunchedEffect(Unit) {
        delay(3000.milliseconds)
        navController.navigate(targetDestination) {
            popUpTo("loading") { inclusive = true }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        MinimalistAnimation()
    }
}
