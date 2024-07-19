package com.example.pokedex

import androidx.annotation.ColorRes

enum class PokemonType(@ColorRes val colorResId: Int) {
    FUEGO(R.color.pokemon_fuego),
    AGUA(R.color.pokemon_agua),
    PLANTA(R.color.pokemon_planta),
    ELECTRICO(R.color.pokemon_electrico),
    HIELO(R.color.pokemon_hielo),
    LUCHA(R.color.pokemon_lucha),
    VENENO(R.color.pokemon_veneno),
    TIERRA(R.color.pokemon_tierra),
    VOLADOR(R.color.pokemon_volador),
    PSIQUICO(R.color.pokemon_psiquico),
    BICHO(R.color.pokemon_bicho),
    ROCA(R.color.pokemon_roca),
    FANTASMA(R.color.pokemon_fantasma),
    SINIESTRO(R.color.pokemon_siniestro),
    DRAGON(R.color.pokemon_dragon),
    ACERO(R.color.pokemon_acero),
    HADA(R.color.pokemon_hada),
    NORMAL(R.color.pokemon_normal)
}