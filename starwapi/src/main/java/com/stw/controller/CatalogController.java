package com.stw.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stw.dto.CatalogDto.CharacterDto;
import com.stw.dto.CatalogDto.FilmDto;
import com.stw.dto.CatalogDto.PageDto;
import com.stw.dto.CatalogDto.StarshipDto;
import com.stw.dto.CatalogDto.VehicleDto;
import com.stw.service.CatalogService;

@RestController 
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService service;
    
    public CatalogController(CatalogService service) { 
        this.service = service; 
    }

    @GetMapping("/characters")
    public PageDto<CharacterDto> characters(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort) { 
        
        return service.characters(name, page, size, sort); 
    }

    @GetMapping("/characters/{id}") 
    public CharacterDto character(@PathVariable long id) { 
        return service.character(id); 
    }

    @GetMapping("/characters/{id}/films") 
    public List<FilmDto> characterFilms(@PathVariable long id) { 
        return service.characterFilms(id); 
    }
    
    @GetMapping("/characters/{id}/vehicles") 
    public List<VehicleDto> characterVehicles(@PathVariable long id) { 
        return service.characterVehicles(id); 
    }
    
    @GetMapping("/characters/{id}/starships") 
    public List<StarshipDto> characterStarships(@PathVariable long id) { 
        return service.characterStarships(id); 
    }

    @GetMapping("/films/{id}/characters") 
    public List<CharacterDto> filmCharacters(@PathVariable long id) { 
        return service.filmCharacters(id);
    }
    
    @GetMapping("/films/{id}/vehicles") 
    public List<VehicleDto> filmVehicles(@PathVariable long id) { 
        return service.filmVehicles(id); 
    }
    
    @GetMapping("/films/{id}/starships") 
    public List<StarshipDto> filmStarships(@PathVariable long id) { 
        return service.filmStarships(id); 
    }

    
}
