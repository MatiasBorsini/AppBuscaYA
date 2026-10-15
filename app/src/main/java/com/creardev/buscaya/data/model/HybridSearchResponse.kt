package com.creardev.buscaya.data.model

import com.google.gson.annotations.SerializedName

data class HybridSearchResponse(
    @SerializedName("results") val results: List<SearchItem> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("location") val location: String = "",
    @SerializedName("message") val message: String = ""
)

data class SearchItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("descripcion") val descripcion: String = "",
    @SerializedName("precio") val precio: String = "",
    @SerializedName("imagen") val imagen: String = "",
    @SerializedName("negocio_nombre") val negocioNombre: String = "",
    @SerializedName("email_contacto") val emailContacto: String = "",
    @SerializedName("telefono") val telefono: String = "",
    @SerializedName("fuente") val fuente: String = "",
    @SerializedName("ubicacion") val ubicacion: String = "",
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("place_id") val placeId: String = ""
) {
    val imagenSegura: String
        get() = if (imagen.startsWith("http://", ignoreCase = true)) {
            "https://${imagen.substringAfter("://")}" 
        } else {
            imagen
        }
}
