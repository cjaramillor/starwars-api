package com.stw.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity 
@Table(name = "vehicles")
public class VehicleEntity extends NamedResource {
    public String model;
    public String manufacturer;
    public String vehicleClass;
}