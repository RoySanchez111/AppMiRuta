package com.example.appbanco.ui.theme

enum class EscalaAccesibilidad(
    val nombre: String,
    val factor: Float,
    val simboloAa: String,
    val descripcion: String
) {
    PEQUEÑO("Pequeño", 0.90f, "aA", "Texto compacto"),
    MEDIANO("Mediano", 1.10f, "Aa", "Texto estándar"),
    GRANDE("Grande", 1.35f, "AA", "Alta legibilidad"),
    EXTRA_GRANDE("Extra Grande", 1.65f, "AA+", "Baja visión");

    companion object {
        fun desdeNombre(nombre: String?): EscalaAccesibilidad {
            return values().find { it.nombre.equals(nombre, ignoreCase = true) } ?: MEDIANO
        }
    }
}
