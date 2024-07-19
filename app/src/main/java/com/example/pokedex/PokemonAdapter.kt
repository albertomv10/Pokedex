package com.example.pokedex

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class PokemonAdapter(private val pokemonList: List<PokemonClass>) :
    RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pokemonImage: ImageView = view.findViewById(R.id.pokemon_image)
        val pokemonName: TextView = view.findViewById(R.id.pokemon_name)
        val typeColorBar: View = view.findViewById(R.id.type_color_bar)
        //val pokemonType: TextView = view.findViewById(R.id.pokemon_type)
        //val pokemonNumber: TextView = view.findViewById(R.id.pokemon_number)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        holder.pokemonName.text = pokemon.name

        Picasso.get().load(pokemon.image).into(holder.pokemonImage)

        /*
        val typeNames = pokemon.type.joinToString(", ") { it.name }
        holder.pokemonType.text = typeNames

        holder.pokemonGeneration.text = "Generación: ${pokemon.generation}"

         */

        // Usar el primer tipo para la barra de color
        val primaryTypeColor = pokemon.type.firstOrNull()?.colorResId ?: R.color.default_type_color
        val color = ContextCompat.getColor(holder.pokemonName.context, primaryTypeColor)

        // Cambiar el color del drawable
        val background = holder.typeColorBar.background as GradientDrawable
        background.setColor(color)

        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context, pokemon.name, Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount() = pokemonList.size
}

fun mapPokemonType(typeName: String): PokemonType {
    return when (typeName.toLowerCase()) {
        "fire" -> PokemonType.FUEGO
        "water" -> PokemonType.AGUA
        "grass" -> PokemonType.PLANTA
        "electric" -> PokemonType.ELECTRICO
        "ice" -> PokemonType.HIELO
        "fighting" -> PokemonType.LUCHA
        "poison" -> PokemonType.VENENO
        "ground" -> PokemonType.TIERRA
        "flying" -> PokemonType.VOLADOR
        "psychic" -> PokemonType.PSIQUICO
        "bug" -> PokemonType.BICHO
        "rock" -> PokemonType.ROCA
        "ghost" -> PokemonType.FANTASMA
        "dark" -> PokemonType.SINIESTRO
        "dragon" -> PokemonType.DRAGON
        "steel" -> PokemonType.ACERO
        "fairy" -> PokemonType.HADA
        "normal" -> PokemonType.NORMAL
        else -> PokemonType.NORMAL // Valor predeterminado si no se encuentra el tipo
    }
}