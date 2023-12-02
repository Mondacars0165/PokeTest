package com.test.poketest

import PokemonApiService
import PokemonListAdapter
import PokemonListItem
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var detailsContainer: FrameLayout
    private lateinit var clockTextView: TextView

    private val pokemonListAdapter = PokemonListAdapter()
    private val apiService = createPokemonApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerView)
        detailsContainer = findViewById(R.id.detailsContainer)
        clockTextView = findViewById(R.id.clockTextView)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = pokemonListAdapter

        // Configurar búsqueda
        searchView.setOnQueryTextListener(this)

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
                        } else {
                            Log.e("MainActivity", "Los detalles del Pokémon son nulos.")
                        }
                    }
                } else {
                    Log.e("MainActivity", "Respuesta no exitosa en la búsqueda: ${response.code()}")
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

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (!query.isNullOrBlank()) {
            searchPokemon(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        // Lógica para realizar la búsqueda mientras se escribe
        // Puedes agregar un límite de tiempo para evitar solicitudes excesivas
        return true
    }
}
