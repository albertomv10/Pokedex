package com.example.pokedex

import com.google.gson.annotations.SerializedName

data class Pokemon(
    val name: String,
    val url: String,
    val id: Int
)

data class PokemonResponse(
    val results: List<Pokemon>
)

data class PokemonDetail(
    val id: Int,
    val name: String,
    val sprites: Sprites,
    val types: List<TypeSlot>
)

data class Sprites(
    val front_default: String,
    val other: OtherSprites
)
data class OtherSprites(
    @SerializedName("official-artwork") val officialArtwork: OfficialArtwork
)
data class OfficialArtwork(
    val front_default: String
)

data class TypeSlot(
    val slot: Int,
    val type: Type,
)

data class Type(
    val name: String
)

data class PokemonSpecies(
    val generation: Generation,
    val name: String,
    val id: Int
)

data class Generation(
    val name: String,
    val url: String
)

data class GenerationDetail(
    val names: List<Idiomas>,
    val pokemon_species: List<PokemonEntrySpecies>
)
data class Idiomas (
    val name: String
)

data class TypeResponse(
    @SerializedName("pokemon") val pokemonEntries: List<PokemonEntry>
)

data class PokemonEntry(
    val pokemon: Pokemon,
    val url: String
)

data class PokemonEntrySpecies(
    val pokemon: PokemonSpecies,
    val url: String
)

