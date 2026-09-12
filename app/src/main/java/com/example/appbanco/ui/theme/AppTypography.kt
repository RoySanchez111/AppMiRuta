package com.example.appbanco.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


fun obtenerTipografiaPersonalizada(escalaFuente: EscalaAccesibilidad = EscalaAccesibilidad.MEDIANO): Typography {
    // Si la opción de accesibilidad está activada, aumenta significativamente el tamaño de letra utilizando el factor centralizado
    val factor = escalaFuente.factor

    return Typography(
        // Titulos
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * factor).sp
        ),

        // Encabezados
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (24 * factor).sp
        ),

        // 3. Tarjetas
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (20 * factor).sp
        ),

        // 4. Tarejtas medianas
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * factor).sp
        ),

        // 5. texto
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * factor).sp
        ),

        // 6. Subitulos
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * factor).sp
        ),

        // 7.Botones
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (14 * factor).sp
        ),

        // 8. Labels
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * factor).sp
        )
    )
}
