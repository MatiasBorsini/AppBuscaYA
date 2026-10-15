package com.creardev.buscaya.data.remote

import com.creardev.buscaya.data.model.FarmaciaResponse
import com.creardev.buscaya.data.model.HybridSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/farmacias_turno_android")
    suspend fun getFarmacias(): FarmaciaResponse

    @GET("api/hybrid-search-android")
    suspend fun hybridSearch(@Query("q") query: String): HybridSearchResponse
}
