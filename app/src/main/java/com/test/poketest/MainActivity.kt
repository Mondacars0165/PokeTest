package com.test.poketest

import PokemonApiService
import PokemonClickListener
import PokemonListAdapter
import PokemonListItem
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class MainActivity : AppCompatActivity(), PokemonClickListener {

    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var detailsContainer: FrameLayout
    private lateinit var clockTextView: TextView
    private lateinit var detailsTextView: TextView

    private lateinit var pokemonListAdapter: PokemonListAdapter
    private val apiService = createPokemonApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        searchEditText = findViewById(R.id.searchEditText)
        recyclerView = findViewById(R.id.recyclerView)
        detailsContainer = findViewById(R.id.detailsContainer)
        clockTextView = findViewById(R.id.clockTextView)
        detailsTextView = findViewById(R.id.detailsTextView)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar el adaptador después de crear el apiService
        pokemonListAdapter = PokemonListAdapter(this)
        recyclerView.adapter = pokemonListAdapter

        // Configurar búsqueda
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No necesitas hacer nada aquí
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Realiza acciones en respuesta a los cambios de texto
                val searchText = s.toString()
                Log.d("MainActivity", "Texto de búsqueda: $searchText")

                // Puedes implementar la lógica de búsqueda aquí
                // Llama a tu función de búsqueda con el texto actual
                searchPokemon(searchText)
            }

            override fun afterTextChanged(s: Editable?) {
                // No necesitas hacer nada aquí
            }
        })

        // Cargar lista de Pokémon inicial
        loadPokemonList()

        // Actualizar el reloj
        updateClock()
    }

    private fun createPokemonApiService(): PokemonApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(PokemonApiService::class.java)
    }

    private fun loadPokemonList() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getPokemonList(0, 1000)

                if (response.isSuccessful) {
                    val pokemonList = response.body()

                    withContext(Dispatchers.Main) {
                        if (pokemonList != null) {
                            val updatedList = pokemonList.results.map { pokemonListItem ->
                                val imageUrl = getPokemonImageUrl(pokemonListItem.url)
                                PokemonListItem(pokemonListItem.name, pokemonListItem.url, imageUrl)
                            }
                            pokemonListAdapter.submitList(updatedList)
                        } else {
                            Log.e("MainActivity", "La lista de Pokémon es nula.")
                        }
                    }
                } else {
                    Log.e("MainActivity", "Respuesta no exitosa: ${response.code()}")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("MainActivity", "Error de red al cargar la lista de Pokémon: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MainActivity", "Error al cargar la lista de Pokémon: ${e.message}")
            }
        }
    }

    private fun searchPokemon(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getPokemonDetails(query)

                if (response.isSuccessful) {
                    val pokemonDetails = response.body()

                    withContext(Dispatchers.Main) {
                        if (pokemonDetails != null) {
                            Log.d("MainActivity", "Detalles del Pokémon: $pokemonDetails")

                            // Inflar la vista del modal
                            val modalView = layoutInflater.inflate(R.layout.modal_pokemon_details, null)
                            val modalImageView: ImageView = modalView.findViewById(R.id.modalPokemonImage)
                            val modalNameTextView: TextView = modalView.findViewById(R.id.modalPokemonName)
                            val modalHeightTextView: TextView = modalView.findViewById(R.id.modalPokemonHeight)
                            val modalWeightTextView: TextView = modalView.findViewById(R.id.modalPokemonWeight)

                            // Configurar la vista del modal con los detalles del Pokémon
                            Glide.with(this@MainActivity)
                                .load(getPokemonImageUrl(pokemonDetails.sprites.frontDefault))
                                .into(modalImageView)

                            modalNameTextView.text = pokemonDetails.name
                            modalHeightTextView.text = "Altura: ${pokemonDetails.height}"
                            modalWeightTextView.text = "Peso: ${pokemonDetails.weight}"

                            // Mostrar el modal
                            detailsContainer.removeAllViews()
                            detailsContainer.addView(modalView)

                        } else {
                            Log.e("MainActivity", "Los detalles del Pokémon son nulos.")
                        }
                    }
                } else {
                    Log.e("MainActivity", "Respuesta no exitosa en la búsqueda: ${response.code()}")
                    // Puedes manejar diferentes códigos de error aquí si es necesario
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("MainActivity", "Error de red al buscar el Pokémon: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MainActivity", "Error al buscar el Pokémon: ${e.message}")
            }
        }
    }

    private fun updateClock() {
        // Implementa la lógica para actualizar el reloj aquí
    }

    private fun getPokemonImageUrl(pokemonUrl: String): String {
        val pokemonId = extractPokemonIdFromUrl(pokemonUrl)
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"
    }

    private fun extractPokemonIdFromUrl(pokemonUrl: String): String {
        val parts = pokemonUrl.split("/")
        return parts[parts.size - 2]
    }

    override fun onPokemonClick(pokemon: PokemonListItem) {
        Log.d("MainActivity", "Clic en el Pokémon: ${pokemon.name}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getPokemonDetails(pokemon.name)

                if (response.isSuccessful) {
                    val pokemonDetails = response.body()

                    withContext(Dispatchers.Main) {
                        if (pokemonDetails != null) {
                            Log.d("MainActivity", "Detalles del Pokémon: $pokemonDetails")

                            // Inflar la vista del modal
                            val modalView = layoutInflater.inflate(R.layout.modal_pokemon_details, null)
                            val modalImageView: ImageView = modalView.findViewById(R.id.modalPokemonImage)
                            val modalNameTextView: TextView = modalView.findViewById(R.id.modalPokemonName)
                            val modalHeightTextView: TextView = modalView.findViewById(R.id.modalPokemonHeight)
                            val modalWeightTextView: TextView = modalView.findViewById(R.id.modalPokemonWeight)

                            // Configurar la vista del modal con los detalles del Pokémon
                            Glide.with(this@MainActivity)
                                .load(getPokemonImageUrl(pokemonDetails.sprites.frontDefault))
                                .into(modalImageView)

                            modalNameTextView.text = pokemonDetails.name
                            modalHeightTextView.text = "Altura: ${pokemonDetails.height}"
                            modalWeightTextView.text = "Peso: ${pokemonDetails.weight}"

                            // Mostrar el modal
                            detailsContainer.removeAllViews()
                            detailsContainer.addView(modalView)

                        } else {
                            Log.e("MainActivity", "Los detalles del Pokémon son nulos.")
                        }
                    }
                } else {
                    Log.e("MainActivity", "Respuesta no exitosa al obtener detalles del Pokémon: ${response.code()}")
                    // Puedes manejar diferentes códigos de error aquí si es necesario
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("MainActivity", "Error de red al obtener detalles del Pokémon: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MainActivity", "Error al obtener detalles del Pokémon: ${e.message}")
            }
        }
    }
}
