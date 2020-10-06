package com.pokedex.rest;

import com.pokedex.model.Pokemon;
import com.pokedex.model.ResponsePokemon;
import com.pokedex.service.PokeAPIImpl;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class RestPokeAPI {

    @Autowired
    private PokeAPIImpl pokeAPI;

    @CrossOrigin
    @Cacheable("pokemonList")
    @GetMapping("/pokemonList/{offset}/{limit}")
    public ResponsePokemon getPokemonOffsetLimit(@PathVariable("offset") int offset, @PathVariable("limit") int limit) {
        List<Pokemon> pokemonResponse = new ArrayList<>();
        Map<String, Object> pokemonMap = null;
        try {
            pokemonMap = pokeAPI.getPokemonListOffsetLimit(offset, limit);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Map<String, String>> pokemonList = (List<Map<String, String>>) pokemonMap.get("results");
        pokemonList.forEach(map -> {
            try {

                String id = FilenameUtils.getName(StringUtils.chop(map.get("url")));
                Pokemon pokemon = pokeAPI.getPokemonBasicInfoById(Integer.parseInt(id), map.get("name"));
                pokemonResponse.add(pokemon);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        ResponsePokemon responsePokemon = new ResponsePokemon();
        responsePokemon.setPokemonList(pokemonResponse);
        String nextMap = (String) pokemonMap.get("next");
        String[] nextMapSplit = nextMap.split("\\?");
        String next = nextMapSplit[1].replaceAll("[^0-9]+", " ");
        List<String> boundariesN = Arrays.asList(next.trim().split(" "));
        responsePokemon.setNext(boundariesN.get(0)+"/"+boundariesN.get(1));
        if(pokemonMap.get("previous") != null) {
            String previousMap = (String) pokemonMap.get("previous");
            String[] previousMapSplit = previousMap.split("\\?");
            String previous = previousMapSplit[1].replaceAll("[^0-9]+", " ");
            List<String> boundariesP = Arrays.asList(previous.trim().split(" "));
            responsePokemon.setPrevious(boundariesP.get(0)+"/"+boundariesP.get(1));
        }

        return responsePokemon;
    }
}
