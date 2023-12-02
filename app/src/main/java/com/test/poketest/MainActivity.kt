package com.test.poketest

import PokemonApiService
import PokemonListAdapter
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


class MainActivity : AppCompatActivity() {

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
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Lógica para realizar la búsqueda cuando se envía el formulario
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



                // Verificar si la respuesta es exitosa después de realizar la llamada
                if (response.isSuccessful) {
                    val pokemonList = response.body()

                    withContext(Dispatchers.Main) {
                        // Verificar si la lista de Pokémon no es nula antes de enviarla al adaptador
                        if (pokemonList != null) {
                            pokemonListAdapter.submitList(pokemonList.results)
                        } else {
                            // Manejar el caso donde la lista es nula
                            Log.e("MainActivity", "La lista de Pokémon es nula.")
                        }
                    }
                } else {
                    // Manejar el caso donde la respuesta no es exitosa
                    Log.e("MainActivity", "Respuesta no exitosa: ${response.code()}")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // Manejar errores de red aquí
                Log.e("MainActivity", "Error de red al cargar la lista de Pokémon: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                // Manejar otros errores aquí
                Log.e("MainActivity", "Error al cargar la lista de Pokémon: ${e.message}")
            }
        }
    }

    private fun searchPokemon(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getPokemonDetails(query)

                // Verificar si la respuesta de búsqueda es exitosa
                if (response.isSuccessful) {
                    val pokemonDetails = response.body()

                    withContext(Dispatchers.Main) {
                        // Puedes manejar los detalles del Pokémon aquí, por ejemplo, mostrar en el detalleContainer
                        if (pokemonDetails != null) {
                            Log.d("MainActivity", "Detalles del Pokémon: $pokemonDetails")
                        } else {
                            // Manejar el caso donde los detalles son nulos
                            Log.e("MainActivity", "Los detalles del Pokémon son nulos.")
                        }
                    }
                } else {
                    // Manejar el caso donde la respuesta no es exitosa
                    Log.e("MainActivity", "Respuesta no exitosa en la búsqueda: ${response.code()}")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // Manejar errores de red aquí
                Log.e("MainActivity", "Error de red al buscar el Pokémon: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                // Manejar otros errores aquí
                Log.e("MainActivity", "Error al buscar el Pokémon: ${e.message}")
            }
        }
    }

    private fun updateClock() {
        // Implementa la lógica para actualizar el reloj aquí
    }
}
