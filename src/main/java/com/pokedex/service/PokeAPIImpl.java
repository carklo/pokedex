package com.pokedex.service;

import com.pokedex.model.Ability;
import com.pokedex.model.Evolution;
import com.pokedex.model.Pokemon;
import com.pokedex.model.Type;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class PokeAPIImpl {

    private PokeAPI service;

    public PokeAPIImpl() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PokeAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(PokeAPI.class);
    }

    public Map<String, Object> getPokemonListOffsetLimit(int offset, int limit) throws IOException {
        Call<Map<String, Object>> retroCallPokemonMap = service.listOfPokemonLimitOffset(offset, limit);
        Response<Map<String, Object>>  response = retroCallPokemonMap.execute();

        if (!response.isSuccessful()) {
            throw new IOException(response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error");
        }
        return response.body();
    }

    public Pokemon getPokemonBasicInfoById(int id, String name) throws IOException {
        Call<Map<String, Object>> retroCallPokemonInfo = service.pokemonInfo(id);
        Response<Map<String, Object>>  response = retroCallPokemonInfo.execute();

        if (!response.isSuccessful()) {
            throw new IOException(response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error");
        }

        return getPokemonFromResponse(id, name, response);
    }

    private Pokemon getPokemonFromResponse(int id, String name, Response<Map<String, Object>> response) throws IOException {
        Pokemon pokemon = new Pokemon();
        pokemon.setName(name);
        pokemon.setId(id);
        List<Ability> abilityList = new ArrayList<>();
        List<Type> typeList = new ArrayList<>();
        List<Map<String, Object>> abilities = (List) response.body().get("abilities");
        abilities.forEach(map -> {
            Map<String, String> abilitiesMap = (Map<String, String>) map.get("ability");
            Ability ability = new Ability();
            ability.setName(abilitiesMap.get("name"));
            abilityList.add(ability);
        });
        List<Map<String, Object>> types = (List) response.body().get("types");
        types.forEach(map -> {
            Map<String, String> typesMap = (Map<String, String>) map.get("type");
            Type type = new Type();
            type.setName(typesMap.get("name"));
            typeList.add(type);
        });
        pokemon.setWeight((Double) response.body().get("weight"));
        pokemon.setTypes(typeList);
        pokemon.setAbilities(abilityList);
        pokemon.setImageURL("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/dream-world/"+id+".svg");
        getDescription(pokemon);
        pokemon.setEvolutions(Collections.EMPTY_LIST);
        getEvolutionChain(pokemon, Collections.EMPTY_LIST);
        return pokemon;
    }

    private void getDescription(Pokemon pokemon) throws IOException {
        Call<Map<String, Object>> retroCallPokemonInfo = service.pokemonCharacteristic(pokemon.getId());
        Response<Map<String, Object>>  response = retroCallPokemonInfo.execute();

        if (!response.isSuccessful()) {
            pokemon.setDescription("Not found");
        } else {
            List<Map<String, Object>> descriptions = (List) response.body().get("descriptions");
            descriptions.forEach(map -> {
                Map<String, String> descriptionsMap = (Map<String, String>) map.get("language");
                if(descriptionsMap.get("name").equals("en")) {
                    pokemon.setDescription((String) map.get("description"));
                }
            });
        }
    }

    private void getEvolutionChain(Pokemon pokemon, List<Map<String, Object>> evolvesTo) throws IOException {
        if(evolvesTo.isEmpty() && pokemon.getEvolutions().isEmpty()) {
            Call<Map<String, Object>> retroCallPokemonInfo = service.pokemonEvolutions(pokemon.getId());
            Response<Map<String, Object>>  response = retroCallPokemonInfo.execute();

            if (!response.isSuccessful()) {
                pokemon.setEvolutions(Collections.EMPTY_LIST);
            } else {
                Map<String, Object> chain = (Map<String, Object>) response.body().get("chain");
                List<Map<String, Object>> evolves_to = (List<Map<String, Object>>) chain.get("evolves_to");
                extractEvolution(pokemon, evolves_to);
            }
        } else {
            extractEvolution(pokemon, evolvesTo);
        }
    }

    private void extractEvolution(Pokemon pokemon, List<Map<String, Object>> evolvesTo) {
        if(!evolvesTo.isEmpty()) {
            evolvesTo.forEach(map -> {
                Map<String, String> evolution = (Map<String, String>) map.get("species");
                Evolution evol = new Evolution();
                evol.setName(evolution.get("name"));
                ArrayList<Evolution> evolutions = new ArrayList<>(pokemon.getEvolutions());
                evolutions.add(evol);
                pokemon.setEvolutions(evolutions);
                try {
                    getEvolutionChain(pokemon, (List) map.get("evolves_to"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
