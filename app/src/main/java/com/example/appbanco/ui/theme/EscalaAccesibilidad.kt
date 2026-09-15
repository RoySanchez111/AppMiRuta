package com.example.appbanco.ui.theme

enum class EscalaAccesibilidad(val nombre: String, val factor: Float) {
    PEQUEÑO("Pequeño", 1.05f),
    MEDIANO("Mediano", 1.15f),
    GRANDE("Grande", 1.30f);

    companion object {
        fun desdeNombre(nombre: String?): EscalaAccesibilidad {
            return values().find { it.nombre.equals(nombre, ignoreCase = true) } ?: MEDIANO
        }
    }
}
