package com.example.pokedex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class PokemonAdapter(private val pokemonList: List<PokemonDetail>) :
    RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pokemonImage: ImageView = view.findViewById(R.id.pokemon_image)
        val pokemonName: TextView = view.findViewById(R.id.pokemon_name)
        val pokemonType: TextView = view.findViewById(R.id.pokemon_type)
        val pokemonNumber: TextView = view.findViewById(R.id.pokemon_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]
        holder.pokemonName.text = pokemon.name.capitalize()
        holder.pokemonNumber.text = "#${pokemon.id}"
        holder.pokemonType.text = pokemon.types.joinToString { it.type.name.capitalize() }

        Picasso.get().load(pokemon.sprites.front_default).into(holder.pokemonImage)
    }

    override fun getItemCount() = pokemonList.size
}