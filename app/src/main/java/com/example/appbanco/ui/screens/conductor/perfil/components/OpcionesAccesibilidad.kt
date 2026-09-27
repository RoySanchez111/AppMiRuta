package com.example.appbanco.ui.screens.conductor.perfil.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OpcionesAccesibilidad() {

    var textoNormal by remember { mutableStateOf(true) }
    var textoBold by remember { mutableStateOf(false) }
    var textoGrande by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        // TÍTULO
        Text(
            text = "Accesibilidad",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // TARJETA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF4A4A4D),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                )
        ) {

            Text(
                text = "Escalado de texto",
                color = Color.White,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // NORMAL
            OpcionAccesibilidad(
                texto = "Normal",
                checked = textoNormal,
                onCheckedChange = {
                    textoNormal = true
                    textoBold = false
                    textoGrande = false
                }
            )

            // BOLD
            OpcionAccesibilidad(
                texto = "Bold",
                checked = textoBold,
                onCheckedChange = {
                    textoNormal = false
                    textoBold = true
                    textoGrande = false
                }
            )

            // GRANDE
            OpcionAccesibilidad(
                texto = "Grande",
                checked = textoGrande,
                onCheckedChange = {
                    textoNormal = false
                    textoBold = false
                    textoGrande = true
                }
            )
        }
    }
}

@Composable
private fun OpcionAccesibilidad(
    texto: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {

        Text(
            text = texto,
            color = Color.White,
            fontSize = 13.sp
        )

        Switch(
            checked = checked,
            onCheckedChange = {
                if (it) {
                    onCheckedChange(true)
                }
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFF26767),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF9EA3AA),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}