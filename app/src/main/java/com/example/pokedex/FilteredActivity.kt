package com.example.pokedex

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.MainActivity.Companion.NO_RESULTS_FOUND
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private lateinit var recyclerView: RecyclerView
private lateinit var adapter: PokemonAdapter
@SuppressLint("StaticFieldLeak")
private lateinit var progressBar: ProgressBar
private val pokemonList = mutableListOf<PokemonClass>()

class FilteredActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filtered)
        progressBar = findViewById(R.id.progress_bar)
        pokemonList.clear()
        initRecyclerView()
        val selectedTypes = intent.getStringArrayListExtra("pokemon_tipo")?.map { mapPokemonTipo(it.toLowerCase()) }
        val selectedGeneration = intent.getStringExtra("pokemon_generacion")

        fetchFilteredPokemonData(selectedGeneration, selectedTypes)
    }

    @OptIn(UnstableApi::class)
    private fun fetchFilteredPokemonData(selectedGeneration: String?, selectedTypes: List<PokemonType>?) {
        progressBar.visibility = ProgressBar.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pokemonByType = mutableSetOf<String>()
                val pokemonByGeneration = mutableSetOf<String>()
                val filteredPokemonNames = mutableListOf<String>()

                // Obtener Pokémon por tipo
                if (!selectedTypes.isNullOrEmpty()) {
                    selectedTypes.forEach { type ->
                        Log.d("FilteredPokemonData", "Fetching Pokémon of type: ${type.nombreIngles}")
                        val typeResponse = RetrofitInstance.api.getPokemonByType(type.nombreIngles.toLowerCase())
                        Log.d("FilteredPokemonData", "Tamaño de la respuesta: ${typeResponse.pokemonEntries.size}")
                        pokemonByType.addAll(typeResponse.pokemonEntries.map { entry ->

                            extractNumberFromUrl(entry.pokemon.url)
                        })
                        Log.d("FilteredPokemonData", "url: ${pokemonByType.first()}")

                    }
                }

                // Obtener Pokémon por generación
                if (selectedGeneration != null && selectedGeneration != "Todas") {
                    Log.d("FilteredPokemonData", "Fetching Pokémon of generation: $selectedGeneration")
                    val generationResponse = RetrofitInstance.api.getPokemonGeneration(selectedGeneration.toLowerCase())
                    Log.d("FilteredPokemonData", "Fetched ${generationResponse.pokemon_species.size} Pokémon of generation $selectedGeneration")

                    pokemonByGeneration.addAll(generationResponse.pokemon_species.map { species ->

                      extractNumberFromUrl(species.url)

                    })

                    Log.d("FilteredPokemonData", "First Pokémon of generation $selectedGeneration: ${pokemonByGeneration.first()}")
                }

                // Filtrar Pokémon por tipo y generación
                filteredPokemonNames.addAll(
                    if (pokemonByType.isNotEmpty()) {
                        if (selectedGeneration != "Todas") {
                            pokemonByType.intersect(pokemonByGeneration).toList()
                        } else {
                            pokemonByType.toList()
                        }
                    } else {
                        pokemonByGeneration.toList()
                    }
                )

                Log.d("FilteredPokemonData", "Filtered Pokémon count: ${filteredPokemonNames.size}")

                // Obtener detalles de los Pokémon filtrados
                val pokemonDetails = filteredPokemonNames.mapNotNull { pokemonName ->
                    async {
                        try {
                            Log.d("FilteredPokemonData", "Fetching details for Pokémon: $pokemonName")
                            val pokemonDetail = RetrofitInstance.api.getPokemonDetail(pokemonName)
                            // Continuar solo si el ID del Pokémon es menor o igual a 1025
                            if (pokemonDetail.id > 1025) return@async null

                            val pokemonTypes = pokemonDetail.types.map { mapPokemonType(it.type.name) }
                            val pokemonSpecies = RetrofitInstance.api.getPokemonSpecies(pokemonDetail.id.toString())
                            val generationDetail = RetrofitInstance.api.getPokemonGeneration(pokemonSpecies.generation.name)

                            PokemonClass(
                                name = pokemonSpecies.name.capitalize(),
                                numPokedex = pokemonDetail.id,
                                type = pokemonTypes,
                                generation = generationDetail.names.getOrNull(5)?.name ?: "Unknown",
                                image = pokemonDetail.sprites.other.officialArtwork.front_default
                            )
                        } catch (e: Exception) {
                            Log.e("FilteredPokemonData", "Error fetching details for Pokémon: $pokemonName", e)
                            null
                        }
                    }
                }.awaitAll().filterNotNull()

                withContext(Dispatchers.Main) {
                    pokemonList.addAll(pokemonDetails)
                    adapter.notifyDataSetChanged()
                    if (pokemonDetails.isEmpty()) {
                        Toast.makeText(this@FilteredActivity, NO_RESULTS_FOUND, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("FilteredPokemonData", "Error during fetching Pokémon data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FilteredActivity, "Excepción", Toast.LENGTH_SHORT).show()
                }
            } finally {
                progressBar.visibility = ProgressBar.GONE
            }
        }

    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view)
        adapter = PokemonAdapter(pokemonList) { searchPokemonName(it) }
        val layoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

    }
    private fun searchPokemonName(searchQuery: String, dialog: Dialog? = null) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pokemonSpecies = RetrofitInstance.api.getPokemonSpecies(searchQuery.toLowerCase().trim())
                val pokemonDetail = RetrofitInstance.api.getPokemonDetail(pokemonSpecies.id.toString())
                val generationDetail = RetrofitInstance.api.getPokemonGeneration(pokemonSpecies.generation.name)

                val pokemonTypes = pokemonDetail.types.map { mapPokemonType(it.type.name) }
                // Manejo de posibles excepciones de índice
                val generationName = generationDetail.names.getOrNull(5)?.name ?: "Unknown"

                val pokemonClass = PokemonClass(
                    name = pokemonSpecies.name.capitalize(),
                    numPokedex = pokemonDetail.id,
                    type = pokemonTypes,
                    generation = generationName,
                    hp = pokemonDetail.stats[0].base_stat,
                    image = pokemonDetail.sprites.other.officialArtwork.front_default
                )

                // Navegar a la pantalla de detalles del Pokémon
                withContext(Dispatchers.Main) {
                    navigateToDetail(pokemonClass)
                    dialog?.dismiss()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@FilteredActivity,
                        NO_RESULTS_FOUND,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun navigateToDetail(pokemon: PokemonClass) {
        val intent = Intent(this, PokemonDetailActivity::class.java)
        intent.putExtra("pokemon_nombre", pokemon.name)
        intent.putExtra("pokemon_id", pokemon.numPokedex)

        // Convertir la lista de PokemonType a una lista de strings
        val tipos = pokemon.type.map { it.nombre }
        intent.putStringArrayListExtra("pokemon_tipo", ArrayList(tipos))

        intent.putExtra("pokemon_generacion", pokemon.generation)
        intent.putExtra("pokemon_imagen", pokemon.image)
        intent.putExtra("pokemon_hp", pokemon.hp)

        startActivity(intent)
    }

    fun extractNumberFromUrl(url: String): String {
        // Utiliza una expresión regular para buscar el número al final del URL
        val regex = """/(\d+)/?$""".toRegex()
        val matchResult = regex.find(url)

        // Si se encuentra un resultado, devuelve el número encontrado
        return matchResult?.groupValues?.get(1) ?: ""
    }
}