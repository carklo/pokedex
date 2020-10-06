package com.pokedex.service;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.Map;

public interface PokeAPI {

    String BASE_URL= "https://pokeapi.co/api/v2/";

    @GET("pokemon")
    Call<Map<String, Object>> listOfPokemonLimitOffset(@Query("offset") int offset, @Query("limit") int limit);

    @GET("pokemon/{Id}")
    Call<Map<String, Object>> pokemonInfo(@Path("Id") int Id);

    @GET("characteristic/{Id}")
    Call<Map<String, Object>> pokemonCharacteristic(@Path("Id") int Id);

    @GET("evolution-chain/{Id}")
    Call<Map<String, Object>> pokemonEvolutions(@Path("Id") int Id);
}
