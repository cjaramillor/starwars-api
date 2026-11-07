package com.stw.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity 
@Table(name = "films")
public class FilmEntity extends NamedResource {
    
    public int episodeId;

    public String director;

    public LocalDate releaseDate;

    @Column(columnDefinition = "text") 
    public String openingCrawl;

    @ManyToMany(mappedBy = "films") 
    public Set<CharacterEntity> characters = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "film_vehicles", 
        joinColumns = @JoinColumn(name = "film_id"), 
        inverseJoinColumns = @JoinColumn(name = "vehicle_id"))
    public Set<VehicleEntity> vehicles = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "film_starships", 
        joinColumns = @JoinColumn(name = "film_id"), 
        inverseJoinColumns = @JoinColumn(name = "starship_id"))
    public Set<StarshipEntity> starships = new HashSet<>();
}