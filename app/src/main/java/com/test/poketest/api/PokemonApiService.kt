import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonApiService {
    @GET("pokemon/{name}")
    suspend fun getPokemonDetails(@Path("name") name: String): Response<PokemonDetails>

    @GET("pokemon/{id}")
    suspend fun getPokemonDetailsById(@Path("id") id: Int): Response<PokemonDetails>

    @GET("pokemon/{id}")
    suspend fun getRandomPokemon(@Path("id") id: Int): Response<PokemonDetails>

    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<PokemonListResponse>
}

object PokemonApi {
    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: PokemonApiService = retrofit.create(PokemonApiService::class.java)
}
