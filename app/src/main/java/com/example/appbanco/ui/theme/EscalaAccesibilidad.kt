package com.example.appbanco.ui.theme

enum class EscalaAccesibilidad(val nombre: String, val factor: Float) {
    PEQUEÑO("Pequeño", 1.25f),
    MEDIANO("Mediano", 1.35f),
    GRANDE("Grande", 1.55f);

    companion object {
        fun desdeNombre(nombre: String?): EscalaAccesibilidad {
            return values().find { it.nombre.equals(nombre, ignoreCase = true) } ?: MEDIANO
        }
    }
}
