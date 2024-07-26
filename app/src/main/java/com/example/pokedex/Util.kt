package com.example.pokedex

import android.app.Activity
import android.app.Dialog
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Util {
    private const val NO_RESULTS_FOUND = "No results found"  // Define esto en Util o pásalo desde la Activity

    fun searchPokemonName(
        searchQuery: String,
        dialog: Dialog? = null,
        activity: Activity,
        navigateToDetail: (PokemonClass) -> Unit
    ) {
        if (Util.isLoading) return
        Util.isLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pokemonDetail = RetrofitInstance.api.getPokemonDetail(searchQuery.toLowerCase())
                val pokemonSpecies = RetrofitInstance.api.getPokemonSpecies(pokemonDetail.name)
                val generationDetail = RetrofitInstance.api.getPokemonGeneration(pokemonSpecies.generation.name)

                val pokemonTypes = pokemonDetail.types.map { mapPokemonType(it.type.name) }
                // Manejo de posibles excepciones de índice
                val generationName = generationDetail.names.getOrNull(5)?.name ?: "Unknown"

                val pokemonClass = PokemonClass(
                    name = pokemonDetail.name.capitalize(),
                    numPokedex = pokemonDetail.id,
                    type = pokemonTypes,
                    generation = generationName,
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
                        activity,
                        NO_RESULTS_FOUND,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                Util.isLoading = false
            }
        }
    }

    private var isLoading = false
}

