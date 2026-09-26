package com.example.appbanco.ui.screens.conductor.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EstadoServicio(
    enServicio: Boolean,
    onEstadoChange: (Boolean) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (enServicio) "En servicio" else "Fuera de servicio",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (enServicio) "Compartiendo ubicación GPS" else "Ubicación pausada",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = enServicio,
                onCheckedChange = onEstadoChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = primaryColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    checkedBorderColor = Color.Transparent,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}
