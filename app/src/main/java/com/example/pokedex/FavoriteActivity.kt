package com.example.pokedex

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.MainActivity.Companion.NO_RESULTS_FOUND
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private lateinit var recyclerView: RecyclerView
private lateinit var adapter: PokemonAdapter

@SuppressLint("StaticFieldLeak")
private val pokemonList = mutableListOf<PokemonClass>()
private lateinit var bottomNavigationView: com.google.android.material.bottomnavigation.BottomNavigationView

class FavoriteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        Log.d("FavoriteActivity", "onCreate called")
        initComponents()
        loadFavorites()
        initListeners()
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            val favoritePokemonIds = getFavoritePokemonIds()
            Log.d("FavoriteActivity", "Favorite Pokemon IDs: $favoritePokemonIds")
            withContext(Dispatchers.Main) {
                if (favoritePokemonIds.isNotEmpty()) {
                    fetchFavoritePokemonData(favoritePokemonIds)
                } else {
                    Toast.makeText(
                        this@FavoriteActivity,
                        "No hay pokemon favoritos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private suspend fun getFavoritePokemonIds(): List<Int> {
        return dataStore.data.map { preferences ->
            val favoriteListKey = stringPreferencesKey("favorite_list")
            val favoritesString = preferences[favoriteListKey] ?: ""

            // Manejar valores vacíos
            if (favoritesString.isBlank()) {
                emptyList()
            } else {
                favoritesString.split(",")
                    .mapNotNull {
                        // Convertir solo si el valor es un número válido
                        it.toIntOrNull()
                    }
            }
        }.first()
    }

    private fun fetchFavoritePokemonData(favoritePokemonIds: List<Int>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (favoritePokemonIds.isNotEmpty()) {
                    val favoritePokemons = mutableListOf<PokemonClass>()
                    val pokemonDetails = favoritePokemonIds.map { pokemonId ->
                        async {
                            val pokemonDetail =
                                RetrofitInstance.api.getPokemonDetail(pokemonId.toString())
                            val pokemonTypes =
                                pokemonDetail.types.map { mapPokemonType(it.type.name) }

                            PokemonClass(
                                name = pokemonDetail.name.capitalize(),
                                numPokedex = pokemonDetail.id,
                                type = pokemonTypes,
                                image = pokemonDetail.sprites.other.officialArtwork.front_default
                            )
                        }
                    }.awaitAll()

                    withContext(Dispatchers.Main) {
                        pokemonDetails.forEach { pokemonClass ->
                            if (!pokemonList.any { it.numPokedex == pokemonClass.numPokedex }) {
                                favoritePokemons.add(pokemonClass)
                            }
                        }
                        if (favoritePokemons.isNotEmpty()) {
                            pokemonList.addAll(favoritePokemons)
                            adapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@FavoriteActivity,
                            "No hay pokemon favoritos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FavoriteActivity, "Excepción", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initComponents() {
        initRecyclerView()
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setSelectedItemId(R.id.Favorites)
    }

    private fun initListeners() {
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                R.id.Favorites -> {
                    true
                }

                R.id.Buscar -> {
                    showSearchDialog()
                    true
                }

                else -> {
                    false
                }
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
                val pokemonSpecies =
                    RetrofitInstance.api.getPokemonSpecies(searchQuery.toLowerCase().trim())
                val pokemonDetail =
                    RetrofitInstance.api.getPokemonDetail(pokemonSpecies.id.toString())
                val generationDetail =
                    RetrofitInstance.api.getPokemonGeneration(pokemonSpecies.generation.name)

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
                        this@FavoriteActivity,
                        NO_RESULTS_FOUND,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showSearchDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_pokemon)
        dialog.show()

        val searchView = dialog.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchPokemonName(it.toLowerCase(), dialog)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        val spinner = dialog.findViewById<Spinner>(R.id.spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.generations,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        val checkBoxTodos: CheckBox = dialog.findViewById(R.id.checkbox_todos)
        val checkBoxNormal: CheckBox = dialog.findViewById(R.id.checkbox_normal)
        val checkBoxFuego: CheckBox = dialog.findViewById(R.id.checkbox_fuego)
        val checkBoxAgua: CheckBox = dialog.findViewById(R.id.checkbox_agua)
        val checkBoxPlanta: CheckBox = dialog.findViewById(R.id.checkbox_planta)
        val checkBoxElectrico: CheckBox = dialog.findViewById(R.id.checkbox_electrico)
        val checkBoxHielo: CheckBox = dialog.findViewById(R.id.checkbox_hielo)
        val checkBoxLucha: CheckBox = dialog.findViewById(R.id.checkbox_lucha)
        val checkBoxVeneno: CheckBox = dialog.findViewById(R.id.checkbox_veneno)
        val checkBoxTierra: CheckBox = dialog.findViewById(R.id.checkbox_tierra)
        val checkBoxVolador: CheckBox = dialog.findViewById(R.id.checkbox_volador)
        val checkBoxPsiquico: CheckBox = dialog.findViewById(R.id.checkbox_psiquico)
        val checkBoxBicho: CheckBox = dialog.findViewById(R.id.checkbox_bicho)
        val checkBoxRoca: CheckBox = dialog.findViewById(R.id.checkbox_roca)
        val checkBoxFantasma: CheckBox = dialog.findViewById(R.id.checkbox_fantasma)
        val checkBoxSiniestro: CheckBox = dialog.findViewById(R.id.checkbox_siniestro)
        val checkBoxDragon: CheckBox = dialog.findViewById(R.id.checkbox_dragon)
        val checkBoxAcero: CheckBox = dialog.findViewById(R.id.checkbox_acero)
        val checkBoxHada: CheckBox = dialog.findViewById(R.id.checkbox_hada)

        val filtrarButton = dialog.findViewById<Button>(R.id.btnFiltrar)


        checkBoxTodos.setOnClickListener {
            if (checkBoxTodos.isChecked) {
                checkBoxNormal.isChecked = true
                checkBoxFuego.isChecked = true
                checkBoxAgua.isChecked = true
                checkBoxPlanta.isChecked = true
                checkBoxElectrico.isChecked = true
                checkBoxHielo.isChecked = true
                checkBoxLucha.isChecked = true
                checkBoxVeneno.isChecked = true
                checkBoxTierra.isChecked = true
                checkBoxVolador.isChecked = true
                checkBoxPsiquico.isChecked = true
                checkBoxBicho.isChecked = true
                checkBoxRoca.isChecked = true
                checkBoxFantasma.isChecked = true
                checkBoxSiniestro.isChecked = true
                checkBoxDragon.isChecked = true
                checkBoxAcero.isChecked = true
                checkBoxHada.isChecked = true
            } else {
                checkBoxNormal.isChecked = false
                checkBoxFuego.isChecked = false
                checkBoxAgua.isChecked = false
                checkBoxPlanta.isChecked = false
                checkBoxElectrico.isChecked = false
                checkBoxHielo.isChecked = false
                checkBoxLucha.isChecked = false
                checkBoxVeneno.isChecked = false
                checkBoxTierra.isChecked = false
                checkBoxVolador.isChecked = false
                checkBoxPsiquico.isChecked = false
                checkBoxBicho.isChecked = false
                checkBoxRoca.isChecked = false
                checkBoxFantasma.isChecked = false
                checkBoxSiniestro.isChecked = false
                checkBoxDragon.isChecked = false
                checkBoxAcero.isChecked = false
                checkBoxHada.isChecked = false
            }
        }

        filtrarButton.setOnClickListener {
            val selectedGenerations = spinner.selectedItem.toString()
            val selectedTypesString = mutableListOf<String>()

            if (checkBoxNormal.isChecked) selectedTypesString.add(getString(R.string.normal))
            if (checkBoxFuego.isChecked) selectedTypesString.add(getString(R.string.fuego))
            if (checkBoxAgua.isChecked) selectedTypesString.add(getString(R.string.agua))
            if (checkBoxPlanta.isChecked) selectedTypesString.add(getString(R.string.planta))
            if (checkBoxElectrico.isChecked) selectedTypesString.add(getString(R.string.electrico))
            if (checkBoxHielo.isChecked) selectedTypesString.add(getString(R.string.hielo))
            if (checkBoxLucha.isChecked) selectedTypesString.add(getString(R.string.lucha))
            if (checkBoxVeneno.isChecked) selectedTypesString.add(getString(R.string.veneno))
            if (checkBoxTierra.isChecked) selectedTypesString.add(getString(R.string.tierra))
            if (checkBoxVolador.isChecked) selectedTypesString.add(getString(R.string.volador))
            if (checkBoxPsiquico.isChecked) selectedTypesString.add(getString(R.string.psiquico))
            if (checkBoxBicho.isChecked) selectedTypesString.add(getString(R.string.bicho))
            if (checkBoxRoca.isChecked) selectedTypesString.add(getString(R.string.roca))
            if (checkBoxFantasma.isChecked) selectedTypesString.add(getString(R.string.fantasma))
            if (checkBoxSiniestro.isChecked) selectedTypesString.add(getString(R.string.siniestro))
            if (checkBoxDragon.isChecked) selectedTypesString.add(getString(R.string.dragon))
            if (checkBoxAcero.isChecked) selectedTypesString.add(getString(R.string.acero))
            if (checkBoxHada.isChecked) selectedTypesString.add(getString(R.string.hada))

            //val selectedTypes = selectedTypesString.map { mapPokemonType(it) }
            if (selectedTypesString.isNotEmpty()) {
                if (selectedGenerations == "Todas" && selectedTypesString.size == 18) {
                    Toast.makeText(this, "Está filtrando todos los pokemon", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    navigateToFiltered(selectedGenerations, selectedTypesString, dialog)
                }
            } else {
                Toast.makeText(this, "Debe seleccionar al menos un tipo", Toast.LENGTH_SHORT).show()
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

    private fun navigateToFiltered(
        selectedGeneration: String,
        selectedTypes: List<String>,
        dialog: Dialog
    ) {
        val intent = Intent(this, FilteredActivity::class.java)
        intent.putExtra("pokemon_generacion", selectedGeneration)
        intent.putStringArrayListExtra("pokemon_tipo", ArrayList(selectedTypes))
        startActivity(intent)
        dialog.dismiss()
    }
}
