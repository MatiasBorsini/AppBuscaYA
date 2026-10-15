package com.creardev.buscaya.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.compose.AsyncImage
import com.creardev.buscaya.R
import com.creardev.buscaya.data.local.AppDatabase
import com.creardev.buscaya.data.model.Farmacia
import com.creardev.buscaya.data.model.SearchItem
import com.creardev.buscaya.data.remote.RetrofitClient
import com.creardev.buscaya.data.repository.FarmaciasRepository
import com.creardev.buscaya.ui.adapters.FarmaciasAdapter

/**
 * Pantalla principal que muestra la lista de farmacias utilizando RecyclerView integrado en Jetpack Compose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmaciasScreen(
    onFarmaciaClick: (Farmacia) -> Unit = {},
    onSearchItemClick: (SearchItem) -> Unit = {},
    context: Context = LocalContext.current,
    farmaciasViewModel: FarmaciasViewModel = viewModel(
        factory = remember(context) {
            val db = AppDatabase.getInstance(context)
            val repo = FarmaciasRepository(
                farmaciaDao = db.farmaciaDao(),
                apiService = RetrofitClient.apiService,
                database = db
            )
            FarmaciasViewModelFactory(repo)
        }
    ),
    searchViewModel: SearchViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current

    // Recolecta el StateFlow de manera segura con el ciclo de vida
    val farmaciasUiState by farmaciasViewModel.uiState.collectAsStateWithLifecycle()
    // Recolecta la consulta de búsqueda local para la base de datos Room
    val localSearchQuery by farmaciasViewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_buscaya_logo),
                            contentDescription = "Logo de BuscaYa",
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "BuscaYa",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { farmaciasViewModel.refrescar(forzar = true) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar datos"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchViewModel.query,
                onValueChange = searchViewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("¿Qué estás buscando?") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchViewModel.query.isNotEmpty()) {
                        IconButton(onClick = searchViewModel::clearQuery) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    searchViewModel.search()
                }),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Button(
                onClick = {
                    focusManager.clearFocus()
                    searchViewModel.search()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Buscar")
            }

            SearchResultsSection(
                state = searchViewModel.uiState,
                onItemClick = onSearchItemClick,
                onRetry = searchViewModel::search
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Farmacias de turno",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { farmaciasViewModel.refrescar(forzar = true) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Forzar actualización"
                    )
                }
            }

            // Campo de búsqueda en tiempo real que consulta la base de datos local Room
            OutlinedTextField(
                value = localSearchQuery,
                onValueChange = farmaciasViewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Filtrar farmacias en la base local...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar localmente en Room") },
                trailingIcon = {
                    if (localSearchQuery.isNotEmpty()) {
                        IconButton(onClick = farmaciasViewModel::clearSearchQuery) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar filtro")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            FarmaciasContent(
                state = farmaciasUiState,
                onFarmaciaClick = onFarmaciaClick,
                onRefresh = { farmaciasViewModel.refrescar(forzar = true) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SearchResultsSection(
    state: SearchUiState,
    onItemClick: (SearchItem) -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        SearchUiState.Idle -> Unit

        SearchUiState.Loading -> Box(
            modifier = Modifier.fillMaxWidth().height(88.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(30.dp))
        }

        is SearchUiState.Error -> Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reintentar")
            }
        }

        is SearchUiState.Success -> Column(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text(
                text = "Resultados de búsqueda (${state.results.size})",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (state.results.isEmpty()) {
                Text(
                    text = "No se encontraron resultados.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = state.results,
                        key = { it.id.ifBlank { it.placeId.ifBlank { it.nombre } } }
                    ) { item ->
                        SearchResultItem(item = item, onClick = { onItemClick(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(item: SearchItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.imagenSegura.isNotBlank()) {
                    AsyncImage(
                        model = item.imagenSegura,
                        contentDescription = item.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(android.R.drawable.ic_menu_gallery),
                        placeholder = painterResource(android.R.drawable.ic_menu_gallery)
                    )
                } else {
                    Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.descripcion.isNotBlank()) {
                    Text(
                        item.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (item.ubicacion.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            item.ubicacion,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            item.rating?.let { rating ->
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, Modifier.size(14.dp), tint = Color(0xFFFFB300))
                        Spacer(Modifier.width(3.dp))
                        Text(rating.toString(), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

/**
 * Componente que integra un RecyclerView tradicional dentro de Jetpack Compose mediante AndroidView.
 */
@Composable
private fun FarmaciasRecyclerView(
    farmacias: List<Farmacia>,
    onFarmaciaClick: (Farmacia) -> Unit,
    modifier: Modifier = Modifier
) {
    val adapter = remember {
        FarmaciasAdapter(onFarmaciaClick = onFarmaciaClick)
    }

    // Actualiza la lista del RecyclerView con submitList cada vez que emite Room
    LaunchedEffect(farmacias) {
        adapter.submitList(farmacias)
    }

    AndroidView(
        factory = { ctx ->
            RecyclerView(ctx).apply {
                layoutManager = LinearLayoutManager(ctx)
                this.adapter = adapter
                clipToPadding = false
            }
        },
        update = { recyclerView ->
            if (recyclerView.adapter != adapter) {
                recyclerView.adapter = adapter
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

/**
 * Muestra el contenido de farmacias según el estado actual de [FarmaciasUiState].
 */
@Composable
private fun FarmaciasContent(
    state: FarmaciasUiState,
    onFarmaciaClick: (Farmacia) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        when (state) {
            FarmaciasUiState.Cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            is FarmaciasUiState.Exito -> {
                if (state.farmacias.isEmpty()) {
                    Text(
                        text = "No se encontraron farmacias en la base de datos local.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FarmaciasRecyclerView(
                        farmacias = state.farmacias,
                        onFarmaciaClick = onFarmaciaClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            is FarmaciasUiState.SinConexion -> Column(modifier = Modifier.fillMaxSize()) {
                // Banner avisando que no hay conexión y se muestran datos locales guardados
                SinConexionBanner(onActualizarClick = onRefresh)

                if (state.farmacias.isEmpty()) {
                    Text(
                        text = "No se encontraron farmacias guardadas con ese nombre.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FarmaciasRecyclerView(
                        farmacias = state.farmacias,
                        onFarmaciaClick = onFarmaciaClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            is FarmaciasUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(12.dp))
                Text(state.mensaje, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reintentar")
                }
            }
        }
    }
}

/**
 * Banner informativo que se despliega cuando la aplicación está fuera de línea.
 */
@Composable
private fun SinConexionBanner(onActualizarClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sin conexión. Mostrando datos guardados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onActualizarClick,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Actualizar", fontSize = 12.sp)
            }
        }
    }
}
