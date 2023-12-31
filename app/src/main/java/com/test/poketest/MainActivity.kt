package com.test.poketest

import PokemonApiService
import PokemonClickListener
import PokemonListAdapter
import PokemonListItem
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), PokemonClickListener {

    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var detailsContainer: FrameLayout
    private lateinit var detailsTextView: TextView

    private lateinit var pokemonListAdapter: PokemonListAdapter
    private val apiService = createPokemonApiService()
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        searchEditText = findViewById(R.id.searchEditText)
        recyclerView = findViewById(R.id.recyclerView)
        detailsTextView = findViewById(R.id.detailsTextView)
        detailsContainer = findViewById(R.id.detailsContainer)
        val textClock = findViewById<TextView>(R.id.textClock)
        recyclerView.layoutManager = LinearLayoutManager(this)
        pokemonListAdapter = PokemonListAdapter(this)
        recyclerView.adapter = pokemonListAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && totalItemCount <= (lastVisibleItem + 5)) {
                    searchPokemon(searchEditText.text.toString())
                }
            }
        })

        // Configuración búsqueda
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                Log.d("MainActivity", "Texto de búsqueda: $searchText")
                searchPokemon(searchText)
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
        // Carga lista de Pokémon inicial
        loadPokemonList()
        updateClock(textClock)
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
                val response = apiService.getPokemonList(0, 12000)

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

    //función de busqueda pokemon
    private fun searchPokemon(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pokemonId = query.toIntOrNull() ?: -1
                val response =
                    if (pokemonId > 0) apiService.getPokemonDetails(pokemonId.toString())
                    else apiService.getPokemonDetailsByName(query)
                if (response.isSuccessful) {
                    val pokemonDetails = response.body()
                    withContext(Dispatchers.Main) {
                        if (pokemonDetails != null) {
                            Log.d("MainActivity", "Detalles del Pokémon: $pokemonDetails")

                            // llenar la vista del modal
                            val modalView = layoutInflater.inflate(R.layout.modal_pokemon_details, null)
                            val modalImageView: ImageView = modalView.findViewById(R.id.modalPokemonImage)
                            val modalNameTextView: TextView = modalView.findViewById(R.id.modalPokemonName)
                            val modalHeightTextView: TextView = modalView.findViewById(R.id.modalPokemonHeight)
                            val modalWeightTextView: TextView = modalView.findViewById(R.id.modalPokemonWeight)
                            val modalAbilitiesTextView: TextView = modalView.findViewById(R.id.modalPokemonAbilities)
                            val modalBaseExperienceTextView: TextView = modalView.findViewById(R.id.modalPokemonBaseExperience)

                            val imageUrl = pokemonDetails.sprites.frontDefault

                            Glide.with(this@MainActivity)
                                .load(imageUrl)
                                .into(modalImageView)

                            modalNameTextView.text = pokemonDetails.name
                            modalHeightTextView.text = " ${pokemonDetails.height}"
                            modalWeightTextView.text = " ${pokemonDetails.weight}"
                            val abilitiesNames = pokemonDetails.abilities.map { it.ability.name }
                            val abilitiesText = abilitiesNames.joinToString(", ")

                            modalAbilitiesTextView.text = " $abilitiesText"
                            modalBaseExperienceTextView.text = " ${pokemonDetails.baseExperience}"

                            // Mostrar el modal
                            runOnUiThread {
                                detailsContainer.removeAllViews()
                                detailsContainer.addView(modalView)
                                val btnCloseModal: Button = modalView.findViewById(R.id.btnCloseModal)
                                btnCloseModal.setOnClickListener {
                                    detailsContainer.removeAllViews()
                                }

                            }

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

    private fun updateClock(textClock: TextView) {
        val handler = Handler(Looper.getMainLooper())

        handler.post(object : Runnable {
            override fun run() {
                try {
                    // Obtener la hora actual
                    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val currentTime = dateFormat.format(Date())
                    textClock.text = "Hora actual: $currentTime"

                    // Programar la próxima actualización después de 1 minuto
                    handler.postDelayed(this, 60000)  // 60000 milisegundos = 1 minuto
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }


    private fun getPokemonImageUrl(pokemonUrl: String): String {
        val pokemonId = extractPokemonIdFromUrl(pokemonUrl)
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"
    }

    private fun extractPokemonIdFromUrl(pokemonUrl: String): String {
        val parts = pokemonUrl.split("/")
        return parts[parts.size - 2]
    }

    private fun getPokemonImageUrlFromOfficialArtwork(pokemonName: String): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${getPokemonIdFromName(pokemonName)}.png"
    }

    private fun getPokemonIdFromName(pokemonName: String): Int {
        val name = pokemonName.toLowerCase()
        val regex = Regex("[^a-zA-Z0-9]")
        val cleanName = regex.replace(name, "")
        return cleanName.hashCode() and 0x7fffffff
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

                            // llenar la vista del modal
                            val modalView = layoutInflater.inflate(R.layout.modal_pokemon_details, null)
                            val modalImageView: ImageView = modalView.findViewById(R.id.modalPokemonImage)
                            val modalNameTextView: TextView = modalView.findViewById(R.id.modalPokemonName)
                            val modalHeightTextView: TextView = modalView.findViewById(R.id.modalPokemonHeight)
                            val modalWeightTextView: TextView = modalView.findViewById(R.id.modalPokemonWeight)
                            val modalAbilitiesTextView: TextView = modalView.findViewById(R.id.modalPokemonAbilities)
                            val modalBaseExperienceTextView: TextView = modalView.findViewById(R.id.modalPokemonBaseExperience)
                            val imageUrl = pokemonDetails.sprites.frontDefault

                            Glide.with(this@MainActivity)
                                .load(imageUrl)
                                .into(modalImageView)

                            modalNameTextView.text = pokemonDetails.name
                            modalHeightTextView.text = " ${pokemonDetails.height}"
                            modalWeightTextView.text = " ${pokemonDetails.weight}"

                            // Obtener solo los nombres de las habilidades
                            val abilitiesNames = pokemonDetails.abilities.map { it.ability.name }
                            val abilitiesText = abilitiesNames.joinToString(", ")

                            modalAbilitiesTextView.text = " $abilitiesText"
                            modalBaseExperienceTextView.text = " ${pokemonDetails.baseExperience}"

                            // Mostrar el modal
                            runOnUiThread {
                                detailsContainer.removeAllViews()
                                detailsContainer.addView(modalView)
                                val btnCloseModal: Button = modalView.findViewById(R.id.btnCloseModal)
                                btnCloseModal.setOnClickListener {
                                    detailsContainer.removeAllViews()
                                }
                            }

                        } else {
                            Log.e("MainActivity", "Los detalles del Pokémon son nulos.")
                        }
                    }
                } else {
                    Log.e("MainActivity", "Respuesta no exitosa al obtener detalles del Pokémon: ${response.code()}")
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
