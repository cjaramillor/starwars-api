package com.stw.entities;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class NamedResource {
    @Id 
    public Long id;
    public String name;
}
