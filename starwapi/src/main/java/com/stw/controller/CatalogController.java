package com.stw.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stw.dto.CatalogDto.CharacterDto;
import com.stw.dto.CatalogDto.PageDto;
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



    
}
