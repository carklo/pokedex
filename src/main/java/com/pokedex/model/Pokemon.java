package com.pokedex.model;

import lombok.Data;

import java.util.List;

@Data
public class Pokemon {

    private int Id;
    private String name;
    private List<Ability> abilities;
    private List<Type> types;
    private double weight;
    private String imageURL;
    private String description;
    private List<Evolution> evolutions;
}
