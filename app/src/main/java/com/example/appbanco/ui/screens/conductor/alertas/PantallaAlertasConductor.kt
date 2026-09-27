package com.example.appbanco.ui.screens.conductor.alertas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appbanco.ui.components.obtenerColoresFondo
import com.example.appbanco.ui.screens.conductor.alertas.components.SelectorTipoAlerta
import com.example.appbanco.ui.screens.conductor.alertas.components.TarjetaAlerta
import com.example.appbanco.ui.screens.conductor.components.BarraNavegacionConductor
import com.example.appbanco.ui.screens.DialogoReporte
import com.example.appbanco.ui.screens.AnimacionReporteExitoso
import com.example.appbanco.ui.screens.Incidencia
import com.example.appbanco.ui.viewmodel.MainViewModel
import com.example.appbanco.logic.reproducirSonidoNotificacion
@Composable
fun PantallaAlertasConductor(
    navController: NavController,viewModel: MainViewModel
) {

    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    var tipoSeleccionado by remember {
        mutableStateOf("ruta")
    }
    var mostrarDialogo by remember {
        mutableStateOf(false)
    }

    var mostrarExito by remember {
        mutableStateOf(false)
    }

    // colores segun el tema tilin
    val coloresFondo = remember {
        obtenerColoresFondo()
    }
    var mostrarAlertaRuta by remember {
        mutableStateOf(true)
    }
    val fondoHorario = remember(coloresFondo) {
        Brush.horizontalGradient(coloresFondo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F1F21))
    ) {

        // ENCABEZADO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = fondoHorario,
                    shape = RoundedCornerShape(
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .heightIn(min = 115.dp)
                    .padding(
                        horizontal = 22.dp,
                        vertical = 20.dp
                    ),
                contentAlignment = Alignment.CenterStart
            ) {

                Column {

                    Text(
                        text = "Alertas",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = "Alertas importantes",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 29.sp
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = "Mantente informado durante tu ruta",
                        color = Color.White.copy(alpha = 0.80f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // CONTENIDO
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "Desliza una alerta a la izquierda para eliminarla",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )
            // selector
            SelectorTipoAlerta(
                seleccionada = tipoSeleccionado,
                onSeleccionar = {
                    tipoSeleccionado = it
                }
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // ALERTAS
            if (tipoSeleccionado == "ruta") {

                if (mostrarAlertaRuta) {

                    TarjetaAlerta(
                        onAplicarDesvio = {
                            Toast.makeText(
                                context,
                                "Desvío sugerido aplicado",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onEliminar = {
                            mostrarAlertaRuta = false

                            Toast.makeText(
                                context,
                                "Alerta eliminada",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay alertas de tu ruta",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

            } else  {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "No hay alertas generales",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            // REPORTAR INCIDENCIA
            OutlinedButton(
                onClick = {
                    mostrarDialogo = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF303033),
                    contentColor = Color.White
                )
            ) {

                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF5A1F)
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Reportar Incidencia",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        // ==========================================
        // NAVEGACIÓN DEL CONDUCTOR
        // ==========================================
        BarraNavegacionConductor(
            navController = navController,
            rutaActual = rutaActual
        )
    }

    // DIÁLOGO PARA REPORTAR INCIDENCIA
    if (mostrarDialogo) {
        DialogoReporte(
            rutasSugeridas = viewModel.servicioHorarios.lineasPublicas.map {
                it.codigo
            },
            onDismiss = {
                mostrarDialogo = false
            },
            onConfirm = { nuevaRuta, nuevaDescripcion ->

                viewModel.agregarIncidencia(
                    Incidencia(
                        tipo = "Reporte Conductor",
                        ruta = nuevaRuta.take(2).uppercase(),
                        titulo = "Incidencia en Ruta $nuevaRuta",
                        descripcion = nuevaDescripcion,
                        tiempo = "Hace un momento",
                        colorEtiqueta = Color(0xFFFF5A1F),
                        colorRuta = Color.Gray,
                        confirmaciones = 1,
                        esGlobal = false
                    )
                )

                mostrarDialogo = false
                mostrarExito = true

                reproducirSonidoNotificacion(context)

                Toast.makeText(
                    context,
                    "Reporte enviado para verificación",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }


    // CONFIRMACIÓN DE REPORTE
    if (mostrarExito) {
        AnimacionReporteExitoso(
            onFinished = {
                mostrarExito = false
            }
        )
    }
}