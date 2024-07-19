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

class PokemonAdapter(private val pokemonList: MutableList<PokemonClass>, private val onItemSelected:(PokemonClass) -> Unit) :
    RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pokemonImage: ImageView = view.findViewById(R.id.pokemon_image)
        val pokemonName: TextView = view.findViewById(R.id.pokemon_name)
        val typeColorBar: View = view.findViewById(R.id.type_color_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        holder.pokemonName.text = pokemon.name
        Picasso.get().load(pokemon.image).into(holder.pokemonImage)

        // Usar el primer tipo para la barra de color
        val primaryTypeColor = pokemon.type.firstOrNull()?.colorResId ?: R.color.default_type_color
        val color = ContextCompat.getColor(holder.pokemonName.context, primaryTypeColor)

        // Cambiar el color del drawable
        val background = holder.typeColorBar.background as GradientDrawable
        background.setColor(color)

        holder.itemView.setOnClickListener {
            onItemSelected(pokemon)
        }

    }

    override fun getItemCount() = pokemonList.size

    fun updateData(newPokemonList: List<PokemonClass>) {
        pokemonList.clear()
        pokemonList.addAll(newPokemonList)
        notifyDataSetChanged()
    }
}

fun mapPokemonTipo(typeName: String): PokemonType {
    return when (typeName.toLowerCase()) {
        "fuego" -> PokemonType.FUEGO
        "agua" -> PokemonType.AGUA
        "planta" -> PokemonType.PLANTA
        "electrico" -> PokemonType.ELECTRICO
        "hielo" -> PokemonType.HIELO
        "lucha" -> PokemonType.LUCHA
        "veneno" -> PokemonType.VENENO
        "tierra" -> PokemonType.TIERRA
        "volador" -> PokemonType.VOLADOR
        "psiquico" -> PokemonType.PSIQUICO
        "bicho" -> PokemonType.BICHO
        "roca" -> PokemonType.ROCA
        "fantasma" -> PokemonType.FANTASMA
        "siniestro" -> PokemonType.SINIESTRO
        "dragon" -> PokemonType.DRAGON
        "acero" -> PokemonType.ACERO
        "hada" -> PokemonType.HADA
        "normal" -> PokemonType.NORMAL
        else -> PokemonType.NORMAL // Valor predeterminado si no se encuentra el tipo
    }
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