package com.creardev.buscaya.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.creardev.buscaya.data.model.Farmacia
import com.creardev.buscaya.data.repository.FarmaciasRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Estados sellados (sealed interface) que representan el estado de la UI de Farmacias.
 */
sealed interface FarmaciasUiState {
    data object Cargando : FarmaciasUiState
    data class Exito(val farmacias: List<Farmacia>) : FarmaciasUiState
    data class SinConexion(val farmacias: List<Farmacia>) : FarmaciasUiState
    data class Error(val mensaje: String, val farmacias: List<Farmacia> = emptyList()) : FarmaciasUiState
}

/**
 * ViewModel que expone el estado mediante un [StateFlow].
 * Observa los datos locales guardados en Room y sincroniza con la API remota.
 * Permite filtrar resultados en tiempo real consultando la base de datos local con Room.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FarmaciasViewModel(
    private val repository: FarmaciasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FarmaciasUiState>(FarmaciasUiState.Cargando)
    val uiState: StateFlow<FarmaciasUiState> = _uiState.asStateFlow()

    // Estado del texto de búsqueda local
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        // Observa los cambios en el texto de búsqueda y consulta directamente a Room.
        // flatMapLatest cancela la consulta anterior y ejecuta una nueva en Room cada vez que cambia el texto.
        viewModelScope.launch {
            _searchQuery
                .flatMapLatest { query ->
                    repository.buscarPorNombre(query)
                }
                .collect { listaFiltrada ->
                    val estadoActual = _uiState.value
                    if (estadoActual is FarmaciasUiState.SinConexion) {
                        _uiState.value = FarmaciasUiState.SinConexion(listaFiltrada)
                    } else if (estadoActual !is FarmaciasUiState.Cargando) {
                        _uiState.value = FarmaciasUiState.Exito(listaFiltrada)
                    } else if (listaFiltrada.isNotEmpty()) {
                        _uiState.value = FarmaciasUiState.Exito(listaFiltrada)
                    }
                }
        }

        // Refresca los datos desde la API al iniciar la pantalla
        refrescar()
    }

    /**
     * Actualiza el texto de búsqueda y dispara la consulta en Room en tiempo real.
     */
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    /**
     * Limpia el campo de búsqueda local.
     */
    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    /**
     * Función para solicitar refrescar los datos desde la API.
     * @param forzar si es verdadero, fuerza la llamada a la red aunque haya datos guardados en Room.
     */
    fun refrescar(forzar: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = FarmaciasUiState.Cargando

            val resultado = repository.refrescar(forzar)

            resultado.fold(
                onSuccess = {
                    val listaActual = repository.buscarPorNombre(_searchQuery.value).first()
                    _uiState.value = FarmaciasUiState.Exito(listaActual)
                },
                onFailure = { excepcion ->
                    // 1. Verificamos si la base de datos local Room realmente tiene datos guardados (sin filtro)
                    val todasLasFarmaciasLocales = repository.farmacias.first()
                    // 2. Obtenemos la lista filtrada según la búsqueda actual para mostrar en pantalla
                    val listaFiltrada = repository.buscarPorNombre(_searchQuery.value).first()

                    if (todasLasFarmaciasLocales.isNotEmpty()) {
                        // Si Room TIENE datos (aunque el filtro actual devuelva 0 coincidencias),
                        // mostramos el estado SinConexion conservando la lista filtrada actual.
                        _uiState.value = FarmaciasUiState.SinConexion(listaFiltrada)
                    } else {
                        // Si la base de datos local está REALMENTE vacía y no hay conexión, mostramos Error
                        val mensaje = if (excepcion is IOException) {
                            "Sin conexión a internet. Conéctese a la red para descargar los datos por primera vez."
                        } else {
                            excepcion.message ?: "Ocurrió un error al cargar las farmacias."
                        }
                        _uiState.value = FarmaciasUiState.Error(mensaje)
                    }
                }
            )
        }
    }
}

/**
 * Factory simple para instanciar [FarmaciasViewModel] con el [FarmaciasRepository].
 */
class FarmaciasViewModelFactory(
    private val repository: FarmaciasRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FarmaciasViewModel::class.java)) {
            return FarmaciasViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel no reconocida: ${modelClass.name}")
    }
}
