# AppBuscaYA

**BuscaYa** es una aplicación Android de directorio local que permite consultar farmacias de turno, buscar comercios y acceder a información detallada de servicios en tiempo real, incluso sin conexión a Internet.

---

## 📱 Descripción del proyecto

La aplicación resuelve el problema de acceder a información de farmacias de turno y servicios locales aun cuando el usuario se encuentre en zonas con poca o nula cobertura de Internet. 

Aplica el patrón **Offline-First**: descarga los datos de la API remota la primera vez que se ejecuta (o cuando el usuario fuerza una actualización) y los guarda en una base de datos local **SQLite mediante Room**. A partir de ese momento, la interfaz consulta siempre la base local, ofreciendo una experiencia instantánea y resiliente.

---

## 🚀 Tecnologías utilizadas

- **Lenguaje:** Kotlin 2.2.10
- **Build System:** Android Gradle Plugin (AGP) 9.2.1 con Gradle Catalog (`libs.versions.toml`)
- **Procesador de Anotaciones:** KSP (Kotlin Symbol Processing) `2.1.0-1.0.29`
- **UI:** Jetpack Compose con Material 3 y `AndroidView`
- **Componente de lista:** AndroidX `RecyclerView` con `ListAdapter`, `DiffUtil` y `LinearLayoutManager`
- **Consumo de API:** Retrofit `2.11.0` + Gson Converter
- **Base de Datos Local:** Room `2.8.5` (SQLite)
- **Concurrencia y Reactividad:** Kotlin Coroutines `1.8.0`, `Flow`, `StateFlow` y `flatMapLatest`
- **Carga de Imágenes:** Coil `2.7.0`
- **Manejo del Ciclo de Vida:** Lifecycle Compose `2.6.1` (`collectAsStateWithLifecycle`)

---

## 🏛️ Arquitectura MVVM

El proyecto sigue la arquitectura recomendada por Google (**MVVM + Single Source of Truth**):

```
com.creardev.buscaya
├── data
│   ├── local
│   │   ├── AppDatabase.kt         # Instancia Singleton de Room Database
│   │   ├── dao/FarmaciaDao.kt     # Consultas SQL nativas con Room
│   │   └── entity/FarmaciaEntity.kt # Entidad SQLite y Mappers DTO <-> Entity
│   ├── model
│   │   ├── FarmaciaResponse.kt    # DTOs para la respuesta de Farmacias
│   │   └── HybridSearchResponse.kt# DTOs para Búsqueda Híbrida
│   ├── remote
│   │   ├── ApiService.kt          # Definición de Endpoints de Retrofit
│   │   └── RetrofitClient.kt      # Cliente Singleton de Retrofit
│   └── repository
│       └── FarmaciasRepository.kt # Única fuente de verdad (Single Source of Truth)
└── ui
    ├── adapters
    │   └── FarmaciasAdapter.kt    # ListAdapter para RecyclerView con DiffUtil
    ├── screens
    │   ├── FarmaciasScreen.kt     # Pantalla principal en Compose con AndroidView
    │   ├── FarmaciasViewModel.kt  # Gestión del estado de UI con StateFlow
    │   ├── FarmaciaDetailScreen.kt# Detalle de farmacia de turno
    │   ├── SearchViewModel.kt     # Lógica de búsqueda general
    │   └── SearchItemDetailScreen.kt # Detalle de resultado de comercio
    └── theme                      # Colores, Tipografías y Temas de Material 3
```

---

## 🌐 Consumo de API REST con Retrofit

La aplicación consume los siguientes endpoints públicos mediante **Retrofit 2**:

- **Farmacias de turno:** `GET https://buscaya.com.ar/api/farmacias_turno_android`
- **Búsqueda híbrida de comercios:** `GET https://buscaya.com.ar/api/hybrid-search-android?q={query}`

### Flujo de datos:
1. Retrofit recibe el JSON remoto y lo deserializa en objetos DTO (`FarmaciaResponse`).
2. El repositorio convierte las clases DTO en entidades de Room (`FarmaciaEntity`).
3. El repositorio ejecuta una transacción atómica (`database.withTransaction`) para refrescar la base de datos local.

---

## 💾 Persistencia local con Room

La base de datos SQLite local (`AppDatabase`) almacena los registros de farmacias en la tabla `farmacias`.

- **Entidad (`FarmaciaEntity`):** Contiene la información de la farmacia junto con una marca de tiempo (`fechaActualizacion`) para auditoría.
- **DAO (`FarmaciaDao`):** Expone funciones reactivas que devuelven `Flow<List<FarmaciaEntity>>`.
- **Transacciones atómicas:** Se utiliza `withTransaction` para asegurar que las operaciones de borrado e inserción sean indivisibles.

---

## 🔍 Búsqueda local mediante SQL LIKE

La búsqueda local de farmacias lee **directamente de la base de datos SQLite** a través del DAO mediante una consulta parametrizada con el operador `LIKE`:

```kotlin
@Query("SELECT * FROM farmacias WHERE nombre LIKE '%' || :query || '%' ORDER BY nombre ASC")
fun buscarPorNombre(query: String): Flow<List<FarmaciaEntity>>
```

### Reactividad en tiempo real:
1. El usuario escribe en el campo de texto de la pantalla principal.
2. `FarmaciasViewModel` recibe el evento y emite el texto mediante `_searchQuery`.
3. La función `flatMapLatest` cancela la consulta anterior y dispara la nueva búsqueda en Room en un hilo de fondo.
4. Room emite el resultado mediante `Flow`, actualizando el `StateFlow` que observa la interfaz.

---

## 📋 RecyclerView con ListAdapter y DiffUtil

Para cumplir con requerimientos de integración mixta de vistas nativas y Compose:

- La lista de farmacias utiliza un **`RecyclerView`** tradicional de AndroidX integrándolo en Compose mediante **`AndroidView`**.
- Utiliza **`FarmaciasAdapter`**, un `ListAdapter` respaldado por **`DiffUtil.ItemCallback`**, calculando las diferencias de la lista en hilos secundarios para re-dibujar solo las tarjetas modificadas.
- El diseño individual de cada tarjeta se define en `res/layout/item_farmacia.xml` utilizando componentes Material 3 (`MaterialCardView`, `ShapeableImageView`).

---

## 📡 Funcionamiento sin conexión (Offline-First)

- Si el dispositivo no tiene conexión a Internet al abrir la app, **Room entrega instantáneamente los datos guardados** en la base de datos local.
- Si el usuario presiona el botón "Actualizar" estando sin conexión:
  - Retrofit falla dentro del bloque `try/catch`.
  - El repositorio preserva los datos existentes en SQLite sin borrar nada.
  - La interfaz conmuta al estado `FarmaciasUiState.SinConexion` y despliega un banner informativo mientras mantiene visible la lista guardada.

---

## 📋 Requisitos para ejecutar el proyecto

- **Android Studio:** Ladybug (2024.2.1) o superior.
- **JDK:** Java Development Kit 17 o 21.
- **Android SDK:** Compile SDK 35, Min SDK 24 (Android 7.0 Nougat o superior).
- Dispositivo físico o emulador Android con Google Play Services.

---

## 🛠️ Instalación y ejecución en Android Studio

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/MatiasBorsini/AppBuscaYA.git
   ```
2. **Abrir en Android Studio:**
   - Selecciona `File` > `Open` y elige la carpeta del proyecto.
3. **Sincronizar Gradle:**
   - Espera a que Android Studio descargue las dependencias y ejecute el **Gradle Sync**.
4. **Ejecutar la aplicación:**
   - Selecciona un emulador o dispositivo físico conectado.
   - Haz clic en el botón **Run 'app'** (`Shift + F10`).

---

## 🔮 Funcionalidades futuras

- Implementación de autenticación de usuarios y login.
- Envío de opiniones y valoraciones mediante peticiones HTTP `POST`.
- Guardado de farmacias y comercios favoritos.
- Integración con mapas interactivos (Google Maps SDK).
