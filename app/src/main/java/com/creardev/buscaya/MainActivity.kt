package com.creardev.buscaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.creardev.buscaya.data.model.Farmacia
import com.creardev.buscaya.data.model.SearchItem
import com.creardev.buscaya.ui.screens.FarmaciaDetailScreen
import com.creardev.buscaya.ui.screens.FarmaciasScreen
import com.creardev.buscaya.ui.screens.SearchItemDetailScreen
import com.creardev.buscaya.ui.theme.BuscayaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuscayaTheme {
                var farmaciaSeleccionada by remember { mutableStateOf<Farmacia?>(null) }
                var resultadoSeleccionado by remember { mutableStateOf<SearchItem?>(null) }

                when {
                    resultadoSeleccionado != null -> {
                        BackHandler { resultadoSeleccionado = null }
                        SearchItemDetailScreen(
                            item = resultadoSeleccionado!!,
                            onBack = { resultadoSeleccionado = null }
                        )
                    }

                    farmaciaSeleccionada != null -> {
                        BackHandler { farmaciaSeleccionada = null }
                        FarmaciaDetailScreen(
                            farmacia = farmaciaSeleccionada!!,
                            onBack = { farmaciaSeleccionada = null }
                        )
                    }

                    else -> FarmaciasScreen(
                        onFarmaciaClick = { farmaciaSeleccionada = it },
                        onSearchItemClick = { resultadoSeleccionado = it }
                    )
                }

            }
        }
    }
}
