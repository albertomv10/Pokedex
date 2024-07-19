package com.example.pokedex

import android.app.Dialog
import android.os.Bundle
import android.widget.SearchView
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
        adapter = PokemonAdapter(pokemonList)
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

    fun fetchPokemonData(searchQuery: String? = null, dialog: Dialog? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = if (searchQuery.isNullOrEmpty()) {
                    // Fetch the default list of Pokémon
                    RetrofitInstance.api.getPokemonList(offset, limit)
                } else {
                    // Fetch Pokémon by name
                    RetrofitInstance.api.getPokemonDetail(searchQuery)
                }

                withContext(Dispatchers.Main) {
                    if (response is PokemonResponse) {
                        response.results.forEach { pokemon ->
                            val pokemonDetail = RetrofitInstance.api.getPokemonDetail(pokemon.name)
                            val pokemonSpecies = RetrofitInstance.api.getPokemonSpecies(pokemon.name)

                            val pokemonTypes = pokemonDetail.types.map { mapPokemonType(it.type.name) }

                            val pokemonClass = PokemonClass(
                                name = pokemonDetail.name.capitalize(),
                                numPokedex = pokemonDetail.id,
                                type = pokemonTypes,
                                generation = pokemonSpecies.generation.name.capitalize(),
                                image = pokemonDetail.sprites.other.officialArtwork.front_default
                            )
                            pokemonList.add(pokemonClass)
                        }
                        adapter.notifyDataSetChanged()
                    } else if (response is PokemonDetail) {
                        val pokemonDetail = response
                        val pokemonSpecies = RetrofitInstance.api.getPokemonSpecies(pokemonDetail.name)
                        val pokemonTypes = pokemonDetail.types.map { mapPokemonType(it.type.name) }

                        val pokemonClass = PokemonClass(
                            name = pokemonDetail.name.capitalize(),
                            numPokedex = pokemonDetail.id,
                            type = pokemonTypes,
                            generation = pokemonSpecies.generation.name.capitalize(),
                            image = pokemonDetail.sprites.other.officialArtwork.front_default
                        )
                        adapter.updateData(listOf(pokemonClass))
                        dialog?.dismiss()
                    } else {
                        Toast.makeText(this@MainActivity, "No se ha obtenido ningún resultado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al obtener datos", Toast.LENGTH_SHORT).show()
                }
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
                    fetchPokemonData(it, dialog)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

}

