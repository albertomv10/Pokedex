package com.example.pokedex

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PokemonAdapter
    private val pokemonList = mutableListOf<PokemonClass>()
    private var isLoading = false
    private var offset = 0
    private val limit = 21

    lateinit var btnNombre:CardView
    lateinit var btnNumero:CardView
    lateinit var btnTipo:CardView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponents()
        initListeners()
        setupScrollListener()
        fetchPokemonData()
    }

    private fun initListeners() {
        btnNombre.setOnClickListener { showSearchDialog() }
    }

    private fun initComponents(){
        initRecyclerView()
        btnNombre = findViewById(R.id.botonNombre)
        btnNumero = findViewById(R.id.botonNumero)
        btnTipo = findViewById(R.id.botonTipo)
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view)
        adapter = PokemonAdapter(pokemonList){navigateToDetail(it)}
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

                withContext(Dispatchers.Main) {
                    if (response.results.isNotEmpty()) {
                        val newPokemons = mutableListOf<PokemonClass>()

                        response.results.forEach { pokemon ->
                            val pokemonDetail = RetrofitInstance.api.getPokemonDetail(pokemon.name)
                            val pokemonSpecies = RetrofitInstance.api.getPokemonSpecies(pokemon.name)
                            val generationDetail = RetrofitInstance.api.getPokemonGeneration(pokemonSpecies.generation.name)

                            val pokemonTypes = pokemonDetail.types.map { mapPokemonType(it.type.name) }

                            val pokemonClass = PokemonClass(
                                name = pokemonDetail.name.capitalize(),
                                numPokedex = pokemonDetail.id,
                                type = pokemonTypes,
                                generation = generationDetail.names[5].name,
                                image = pokemonDetail.sprites.other.officialArtwork.front_default
                            )

                            if (!pokemonList.any { it.numPokedex == pokemonClass.numPokedex }) {
                                newPokemons.add(pokemonClass)
                            }
                        }

                        if (newPokemons.isNotEmpty()) {
                            pokemonList.addAll(newPokemons)
                            adapter.notifyDataSetChanged()
                            offset += limit
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "No se ha obtenido ningún resultado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al obtener datos", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    private fun searchPokemonData(searchQuery: String, dialog: Dialog? = null) {
        if (isLoading) return
        isLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pokemonDetail = RetrofitInstance.api.getPokemonDetail(searchQuery)

                withContext(Dispatchers.Main) {
                    if (pokemonDetail != null) {
                        val pokemonSpecies = RetrofitInstance.api.getPokemonSpecies(pokemonDetail.name)
                        val pokemonTypes = pokemonDetail.types.map { mapPokemonType(it.type.name) }
                        val generationDetail = RetrofitInstance.api.getPokemonGeneration(pokemonSpecies.generation.name)


                        val pokemonClass = PokemonClass(
                            name = pokemonDetail.name.capitalize(),
                            numPokedex = pokemonDetail.id,
                            type = pokemonTypes,
                            generation = generationDetail.names[5].name,
                            image = pokemonDetail.sprites.other.officialArtwork.front_default
                        )

                        pokemonList.clear()  // Limpiar la lista antes de añadir el resultado de la búsqueda
                        pokemonList.add(pokemonClass)
                        adapter.notifyDataSetChanged()

                        dialog?.dismiss()
                    } else {
                        Toast.makeText(this@MainActivity, "No se encontró ningún Pokémon", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al obtener datos", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    private fun showSearchDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_pokemon)
        dialog.show()

        val searchView = dialog.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchPokemonData(it.toLowerCase(), dialog)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun navigateToDetail(pokemon: PokemonClass) {
        val intent = Intent(this, PokemonDetailActivity::class.java)
        intent.putExtra("pokemon_nombre", pokemon.name)
        intent.putExtra("pokemon_id", pokemon.numPokedex)

        // Convertir la lista de PokemonType a una lista de strings
        val tipos = pokemon.type.map { it.name }
        intent.putStringArrayListExtra("pokemon_tipo", ArrayList(tipos))

        intent.putExtra("pokemon_generacion", pokemon.generation)
        intent.putExtra("pokemon_imagen", pokemon.image)

        startActivity(intent)
    }
}

