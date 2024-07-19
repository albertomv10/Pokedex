package com.example.pokedex

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

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
        btnNombre.setOnClickListener { Toast.makeText(this, "Prueba", Toast.LENGTH_SHORT).show() }
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

    private fun fetchPokemonData() {
        isLoading = true
        lifecycleScope.launch {
            val response = RetrofitInstance.api.getPokemonList(offset, limit)
            if (response.results.isNotEmpty()) {
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
                offset += limit
                adapter.notifyDataSetChanged()
            }
            isLoading = false
        }
    }

}

