package com.example.pokedex

import com.google.gson.annotations.SerializedName

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
    val names: List<LocalizedName>

)

data class Type(
    val name: String
)

data class LocalizedName(
    val name: String
)

data class PokemonSpecies(
    val generation: Generation
)

data class Generation(
    val name: String,
    val url: String
)