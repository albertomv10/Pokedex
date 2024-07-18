package com.example.pokedex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PokemonAdapter
    private val pokemonList = mutableListOf<PokemonDetail>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        adapter = PokemonAdapter(pokemonList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchPokemonData()
    }

    private fun fetchPokemonData() {
        lifecycleScope.launch {
            val response = RetrofitInstance.api.getPokemonList()
            response.results.forEach { pokemon ->
                val pokemonDetail = RetrofitInstance.api.getPokemonDetail(pokemon.name)
                pokemonList.add(pokemonDetail)
            }
            adapter.notifyDataSetChanged()
        }
    }
}
