package com.pokedex.model;

import lombok.Data;

import java.util.List;

@Data
public class ResponsePokemon {

    private List<Pokemon> pokemonList;
    private String previous;
    private String next;
}
