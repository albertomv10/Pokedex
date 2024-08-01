package com.example.pokedex

import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore("pokemon_data")

class PokemonDetailActivity : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var txtname: TextView
    lateinit var txtTipo1: TextView
    lateinit var txtTipo2: TextView
    lateinit var txtNumPokedex: TextView
    lateinit var txtGeneracion: TextView
    lateinit var txtHp: TextView
    lateinit var favoriteButton: ImageButton
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon_detail)
        initComponents()

        val url = intent.getStringExtra("pokemon_imagen")
        val nombre = intent.getStringExtra("pokemon_nombre")
        val pokemonId = intent.getIntExtra("pokemon_id", -1)
        val pokemonTipos = intent.getStringArrayListExtra("pokemon_tipo")
            ?.map { mapPokemonTipo(it) }
        val pokemonGeneracion = intent.getStringExtra("pokemon_generacion")
        val pokemonHp = intent.getIntExtra("pokemon_hp", 0)

        Picasso.get().load(url).into(imageView)
        txtname.text = nombre
        txtNumPokedex.text = "#${pokemonId}"
        txtGeneracion.text = pokemonGeneracion
        txtHp.text = "HP: ${pokemonHp}"

        isFavorite = getFavoriteStatus(pokemonId)
        if (isFavorite){
            updateFavoriteIcon(favoriteButton, isFavorite)
        }

        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteIcon(favoriteButton, isFavorite)
            lifecycleScope.launch(Dispatchers.IO) {
                saveFavoriteStatus(isFavorite, pokemonId)
            }
        }

        if (pokemonTipos != null && pokemonTipos.isNotEmpty()) {
            val tipo1LayoutParams = txtTipo1.layoutParams as LinearLayout.LayoutParams
            val tipo2LayoutParams = txtTipo2.layoutParams as LinearLayout.LayoutParams

            if (pokemonTipos.size > 1) {
                txtTipo1.text = pokemonTipos[0].nombre
                txtTipo2.text = pokemonTipos[1].nombre
                txtTipo1.setBackgroundColor(ContextCompat.getColor(this, pokemonTipos[0].colorResId))
                txtTipo2.setBackgroundColor(ContextCompat.getColor(this, pokemonTipos[1].colorResId))
                tipo1LayoutParams.weight = 1f
                tipo2LayoutParams.weight = 1f
                txtTipo2.visibility = TextView.VISIBLE
            } else {
                txtTipo1.text = pokemonTipos[0].nombre
                txtTipo1.setBackgroundColor(ContextCompat.getColor(this, pokemonTipos[0].colorResId))
                tipo1LayoutParams.weight = 1f
                txtTipo2.visibility = TextView.GONE
            }

            txtTipo1.layoutParams = tipo1LayoutParams
            txtTipo2.layoutParams = tipo2LayoutParams
        }
    }

    private suspend fun saveFavoriteStatus(favorite: Boolean, pokemonId: Int) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(pokemonId.toString())] = favorite
        }
    }

    private fun getFavoriteStatus(pokemonId: Int): Boolean {
        var favorite = false
        lifecycleScope.launch(Dispatchers.IO) {
            dataStore.data.collect { preferences ->
                favorite = preferences[booleanPreferencesKey(pokemonId.toString())] ?: false
            }
    }
        return favorite
    }

    private fun updateFavoriteIcon(button: ImageButton, isFavorite:Boolean) {
        if (isFavorite) {
            button.setImageResource(R.drawable.ic_corazon_on)
        } else {
            button.setImageResource(R.drawable.ic_corazon_off)
        }
    }

    private fun initComponents() {
        imageView = findViewById(R.id.image)
        txtname = findViewById(R.id.text_name)
        txtTipo1 = findViewById(R.id.tipo1)
        txtTipo2 = findViewById(R.id.tipo2)
        txtNumPokedex = findViewById(R.id.numPokedex)
        txtGeneracion = findViewById(R.id.text_generation)
        txtHp = findViewById(R.id.hp)
        favoriteButton = findViewById(R.id.favorite_button)

    }
}