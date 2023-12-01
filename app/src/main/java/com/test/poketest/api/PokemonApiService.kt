package com.test.poketest.api

import PokemonDetailsResponse
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.converter.gson.GsonConverterFactory


interface PokemonApiService {
    @GET("pokemon/{pokemonId}")
    suspend fun getPokemonDetails(@Path("pokemonId") pokemonId: String): PokemonDetailsResponse
}

object PokemonApi {
    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val pokemonApiService: PokemonApiService = retrofit.create(PokemonApiService::class.java)
}
