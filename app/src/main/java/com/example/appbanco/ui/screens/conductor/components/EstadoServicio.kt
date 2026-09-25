package com.example.appbanco.ui.screens.conductor.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background

@Composable
fun EstadoServicio(
    enServicio: Boolean,
    onEstadoChange: (Boolean) -> Unit
) {

    val verdeServicio = Color(0xFF12805C)
    val naranja = Color(0xFFE59A00)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(verdeServicio)
            .padding(
                horizontal = 14.dp,
                vertical = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = if (enServicio) {
                    "En servicio"
                } else {
                    "Fuera de servicio"
                },
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = if (enServicio) {
                    "Compartiendo ubicación"
                } else {
                    "Ubicación pausada"
                },
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
        }

        Switch(
            checked = enServicio,
            onCheckedChange = onEstadoChange,

            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = naranja,

                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray,

                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}