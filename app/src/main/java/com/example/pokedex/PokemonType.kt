package com.example.pokedex

import androidx.annotation.ColorRes

enum class PokemonType(@ColorRes val colorResId: Int, val nombre: String) {
    FUEGO(R.color.pokemon_fuego, "FUEGO"),
    AGUA(R.color.pokemon_agua, "AGUA"),
    PLANTA(R.color.pokemon_planta, "PLANTA"),
    ELECTRICO(R.color.pokemon_electrico, "ELÉCTRICO"),
    HIELO(R.color.pokemon_hielo, "HIELO"),
    LUCHA(R.color.pokemon_lucha, "LUCHA"),
    VENENO(R.color.pokemon_veneno, "VENENO"),
    TIERRA(R.color.pokemon_tierra, "TIERRA"),
    VOLADOR(R.color.pokemon_volador, "VOLADOR"),
    PSIQUICO(R.color.pokemon_psiquico, "PSÍQUICO"),
    BICHO(R.color.pokemon_bicho, "BICHO"),
    ROCA(R.color.pokemon_roca, "ROCA"),
    FANTASMA(R.color.pokemon_fantasma, "FANTASMA"),
    SINIESTRO(R.color.pokemon_siniestro, "SINIESTRO"),
    DRAGON(R.color.pokemon_dragon, "DRAGÓN"),
    ACERO(R.color.pokemon_acero, "ACERO"),
    HADA(R.color.pokemon_hada, "HADA"),
    NORMAL(R.color.pokemon_normal, "NORMAL")
}