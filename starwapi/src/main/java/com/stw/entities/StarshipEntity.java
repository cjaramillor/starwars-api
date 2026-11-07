package com.stw.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity 
@Table(name = "starships")
public class StarshipEntity extends NamedResource {
    public String model;
    public String manufacturer;
    public String starshipClass;
}