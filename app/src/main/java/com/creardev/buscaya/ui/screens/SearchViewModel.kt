package com.creardev.buscaya.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creardev.buscaya.data.model.SearchItem
import com.creardev.buscaya.data.remote.RetrofitClient
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val results: List<SearchItem>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel : ViewModel() {
    var query by mutableStateOf("")
        private set

    var uiState: SearchUiState by mutableStateOf(SearchUiState.Idle)
        private set

    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        query = value
    }

    fun clearQuery() {
        query = ""
        uiState = SearchUiState.Idle
    }

    fun search() {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            uiState = SearchUiState.Error("Ingresá qué estás buscando.")
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            uiState = SearchUiState.Loading
            uiState = try {
                val response = RetrofitClient.apiService.hybridSearch(normalizedQuery)
                SearchUiState.Success(response.results)
            } catch (_: IOException) {
                SearchUiState.Error("No se pudo conectar. Revisá tu conexión a internet.")
            } catch (error: HttpException) {
                SearchUiState.Error("El servidor respondió con el error ${error.code()}.")
            } catch (_: Exception) {
                SearchUiState.Error("Ocurrió un error al realizar la búsqueda.")
            }
        }
    }
}
