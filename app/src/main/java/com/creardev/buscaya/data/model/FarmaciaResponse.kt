package com.creardev.buscaya.data.model

import com.google.gson.annotations.SerializedName

data class FarmaciaResponse(
    @SerializedName("farmacias")
    val farmacias: List<Farmacia>
)

data class Farmacia(
    @SerializedName("fecha_raw")
    val fechaRaw: String,
    @SerializedName("fecha_iso")
    val fechaIso: String,
    @SerializedName("dia_semana")
    val diaSemana: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("direccion")
    val direccion: String,
    @SerializedName("imagen")
    val imagen: String
) {
    val imagenSegura: String
        get() = if (imagen.startsWith("http://", ignoreCase = true)) {
            "https://${imagen.substringAfter("://")}" 
        } else {
            imagen
        }
}
