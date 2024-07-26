package com.example.pokedex

import androidx.annotation.ColorRes

enum class PokemonType(@ColorRes val colorResId: Int, val nombre: String, val nombreIngles: String) {
    FUEGO(R.color.pokemon_fuego, "FUEGO", "FIRE"),
    AGUA(R.color.pokemon_agua, "AGUA", "WATER"),
    PLANTA(R.color.pokemon_planta, "PLANTA", "GRASS"),
    ELECTRICO(R.color.pokemon_electrico, "ELÉCTRICO", "ELECTRIC"),
    HIELO(R.color.pokemon_hielo, "HIELO", "ICE"),
    LUCHA(R.color.pokemon_lucha, "LUCHA", "FIGHTING"),
    VENENO(R.color.pokemon_veneno, "VENENO", "POISON"),
    TIERRA(R.color.pokemon_tierra, "TIERRA", "GROUND"),
    VOLADOR(R.color.pokemon_volador, "VOLADOR", "FLYING"),
    PSIQUICO(R.color.pokemon_psiquico, "PSÍQUICO", "PSYCHIC"),
    BICHO(R.color.pokemon_bicho, "BICHO", "BUG"),
    ROCA(R.color.pokemon_roca, "ROCA", "ROCK"),
    FANTASMA(R.color.pokemon_fantasma, "FANTASMA", "GHOST"),
    SINIESTRO(R.color.pokemon_siniestro, "SINIESTRO", "DARK"),
    DRAGON(R.color.pokemon_dragon, "DRAGÓN", "DRAGON"),
    ACERO(R.color.pokemon_acero, "ACERO", "STEEL"),
    HADA(R.color.pokemon_hada, "HADA", "FAIRY"),
    NORMAL(R.color.pokemon_normal, "NORMAL", "NORMAL")
}