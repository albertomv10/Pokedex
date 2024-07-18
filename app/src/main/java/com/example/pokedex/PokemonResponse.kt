package com.example.pokedex

data class PokemonResponse(
    val results: List<Pokemon>
)

data class Pokemon(
    val name: String,
    val url: String
)

data class PokemonDetail(
    val id: Int,
    val name: String,
    val sprites: Sprites,
    val types: List<TypeSlot>
)

data class Sprites(
    val front_default: String
)

data class TypeSlot(
    val slot: Int,
    val type: Type
)

data class Type(
    val name: String
)

