package com.stw.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity 
@Table(name = "characters")
public class CharacterEntity extends NamedResource {
    
    public String birthYear;
    public String gender;   
    
    @ManyToMany
    @JoinTable(
        name = "character_films", 
        joinColumns = @JoinColumn(
            name = "character_id"), 
        inverseJoinColumns = @JoinColumn(name = "film_id"))
    public Set<FilmEntity> films = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "character_vehicles", 
        joinColumns = @JoinColumn(
            name = "character_id"), 
            inverseJoinColumns = @JoinColumn(name = "vehicle_id"))
    public Set<VehicleEntity> vehicles = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "character_starships", 
        joinColumns = @JoinColumn(
            name = "character_id"), 
            inverseJoinColumns = @JoinColumn(name = "starship_id"))
    public Set<StarshipEntity> starships = new HashSet<>();
}
