package com.example.appbanco.ui.screens.conductor.alertas.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectorTipoAlerta(
    seleccionada: String,
    onSeleccionar: (String) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
    ) {

        // DE MI RUTA
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    color = if (seleccionada == "ruta")
                        primaryColor
                    else
                        Color.Transparent,
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable {
                    onSeleccionar("ruta")
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "De mi ruta",
                color = if (seleccionada == "ruta") Color.White else MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // GENERALES
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    color = if (seleccionada == "generales")
                        primaryColor
                    else
                        Color.Transparent,
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable {
                    onSeleccionar("generales")
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Generales",
                color = if (seleccionada == "generales") Color.White else MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}