package com.example.pokedex

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso

class PokemonDetailActivity : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var txtname: TextView
    lateinit var txtTipo1: TextView
    lateinit var txtTipo2: TextView
    lateinit var txtGeneracion: TextView


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

        Picasso.get().load(url).into(imageView)
        txtname.text = nombre
        txtGeneracion.text = pokemonGeneracion

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

    private fun initComponents() {
        imageView = findViewById(R.id.image)
        txtname = findViewById(R.id.text_name)
        txtTipo1 = findViewById(R.id.tipo1)
        txtTipo2 = findViewById(R.id.tipo2)
        txtGeneracion = findViewById(R.id.text_generation)

    }
}