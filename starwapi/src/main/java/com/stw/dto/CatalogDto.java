package com.stw.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.stw.entities.CharacterEntity;
import com.stw.entities.FilmEntity;
import com.stw.entities.StarshipEntity;
import com.stw.entities.VehicleEntity;

public class CatalogDto {
    private CatalogDto() {}
    public record CharacterDto(
        Long id, 
        String name, 
        String birthYear, 
        String gender
    ) {
        public static CharacterDto from(CharacterEntity e) { 
            return new CharacterDto(e.id, e.name, e.birthYear, e.gender); 
        }
    }
    public record PageDto<T>(
        List<T> content, 
        int page, 
        int size, 
        long totalElements, 
        int totalPages
    ) {
        public static <T> PageDto<T> from(Page<T> p) { 
            return new PageDto<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()); 
        }
    }
}
