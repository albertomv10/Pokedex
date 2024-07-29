package com.example.pokedex

data class PokemonClass(
    val name: String,
    val numPokedex: Int,
    val type: List<PokemonType>,
    val generation: String = "",
    val hp: Int = 0,
    val image: String) {
    init {
        require(name.isNotBlank()) { "El nombre no puede estar en blanco" }
        require(numPokedex > 0) { "El número de la Pokédex debe ser mayor que 0" }
        require(image.isNotBlank()) { "La URL de la imagen no puede estar en blanco" }
    }
}