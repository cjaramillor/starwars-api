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

    public record FilmDto(
        Long id, 
        String title,
        int episodeId, 
        String director, 
        LocalDate releaseDate, 
        String openingCrawl) {
        public static FilmDto from(FilmEntity e) { 
            return new FilmDto(e.id, e.name, e.episodeId, e.director, e.releaseDate, e.openingCrawl); 
        }
    }

    public record VehicleDto(
        Long id, 
        String name, 
        String model, 
        String manufacturer, 
        String vehicleClass) {
        public static VehicleDto from(VehicleEntity e) { 
            return new VehicleDto(e.id, e.name, e.model, e.manufacturer, e.vehicleClass); 
        }
    }
    public record StarshipDto(
        Long id, 
        String name, 
        String model, 
        String manufacturer, 
        String starshipClass) {
        public static StarshipDto from(StarshipEntity e) { 
            return new StarshipDto(e.id, e.name, e.model, e.manufacturer, e.starshipClass); }
    }

}
