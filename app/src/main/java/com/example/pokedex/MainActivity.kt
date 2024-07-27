package com.example.pokedex

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val BASE_URL = "https://pokeapi.co/api/v2/"
        const val NO_RESULTS_FOUND = "No se encontraron resultados"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PokemonAdapter
    private val pokemonList = mutableListOf<PokemonClass>()
    private var isLoading = false
    private var offset = 0
    private val limit = 21

    lateinit var logo: ImageView
    lateinit var btnFiltrar: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponents()
        initListeners()
        setupScrollListener()
        fetchPokemonData()
    }

    private fun initListeners() {
        btnFiltrar.setOnClickListener { showSearchDialog() }
    }

    private fun initComponents() {
        initRecyclerView()
        logo = findViewById(R.id.logo)
        btnFiltrar = findViewById(R.id.btnFiltrar)
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view)
        adapter = PokemonAdapter(pokemonList) { searchPokemonName(it) }
        val layoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        //recyclerView.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
    }

    private fun setupScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && totalItemCount <= (lastVisibleItem + limit / 2)) {
                    fetchPokemonData()
                }
            }
        })
    }

    private fun fetchPokemonData() {
        if (isLoading) return
        isLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getPokemonList(offset, limit)

                if (response.results.isNotEmpty()) {
                    val newPokemons = mutableListOf<PokemonClass>()

                    val pokemonDetails = response.results.map { pokemon ->
                        async {
                            val pokemonDetail = RetrofitInstance.api.getPokemonDetail(pokemon.name)
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
                                newPokemons.add(pokemonClass)
                            }
                        }

                        if (newPokemons.isNotEmpty()) {
                            pokemonList.addAll(newPokemons)
                            adapter.notifyDataSetChanged()
                            offset += limit
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                NO_RESULTS_FOUND,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            NO_RESULTS_FOUND,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, NO_RESULTS_FOUND, Toast.LENGTH_SHORT)
                        .show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    private fun searchPokemonName(searchQuery: String, dialog: Dialog? = null) {
        if (isLoading) return
        isLoading = true

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
                        this@MainActivity,
                        NO_RESULTS_FOUND,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    /*
    private fun fetchFilteredPokemonData(selectedGeneration: String, selectedTypes: List<PokemonType>, dialog: Dialog? = null) {
        if (isLoading) return
        isLoading = true
        offset=0

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getPokemonList(offset, limit)

                if (response.results.isNotEmpty()) {
                    val newPokemons = mutableListOf<PokemonClass>()

                    val pokemonDetails = response.results.map { pokemon ->
                        async {
                            val pokemonDetail = RetrofitInstance.api.getPokemonDetail(pokemon.name)
                            val pokemonSpecies = RetrofitInstance.api.getPokemonSpecies(pokemonDetail.name)
                            val generationDetail = RetrofitInstance.api.getPokemonGeneration(pokemonSpecies.generation.name)
                            val pokemonTypes = pokemonDetail.types.map { mapPokemonType(it.type.name) }

                            PokemonClass(
                                name = pokemonDetail.name.capitalize(),
                                numPokedex = pokemonDetail.id,
                                type = pokemonTypes,
                                generation = generationDetail.names.getOrNull(5)?.name ?: "Unknown",
                                image = pokemonDetail.sprites.other.officialArtwork.front_default
                            )
                        }
                    }.awaitAll()

                    withContext(Dispatchers.Main) {
                        pokemonDetails.forEach { pokemonClass ->
                            if (pokemonClass.generation == selectedGeneration && selectedTypes.any { it in pokemonClass.type }) {
                                if (!pokemonList.any { it.numPokedex == pokemonClass.numPokedex }) {
                                    newPokemons.add(pokemonClass)
                                }
                            }
                        }

                        if (newPokemons.isNotEmpty()) {
                            pokemonList.clear()
                            pokemonList.addAll(newPokemons)
                            adapter.notifyDataSetChanged()
                            offset += limit
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                NO_RESULTS_FOUND,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        dialog?.dismiss()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "La API no devuelve resultados",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Excepción", Toast.LENGTH_SHORT)
                        .show()
                }
            } finally {
                isLoading = false
            }
        }
    }
     */

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
        ArrayAdapter.createFromResource(this, R.array.generations, android.R.layout.simple_spinner_item).also {
                adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

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

            if (selectedGenerations == "Todas" && selectedTypesString.size == 18){
                Toast.makeText(this, "Está filtrando todos los pokemon", Toast.LENGTH_SHORT).show()
            }else{
                navigetToFiltered(selectedGenerations, selectedTypesString, dialog)
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

        startActivity(intent)
    }

    private fun navigetToFiltered(selectedGeneration: String, selectedTypes: List<String>, dialog: Dialog){

        val intent = Intent(this, FilteredActivity::class.java)
        intent.putExtra("pokemon_generacion", selectedGeneration)
        intent.putStringArrayListExtra("pokemon_tipo", ArrayList(selectedTypes))
        startActivity(intent)
        dialog.dismiss()
    }
}

