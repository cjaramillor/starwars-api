package com.stw.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stw.commons.ApiException;
import com.stw.dto.CatalogDto.CharacterDto;
import com.stw.dto.CatalogDto.FilmDto;
import com.stw.dto.CatalogDto.PageDto;
import com.stw.dto.CatalogDto.StarshipDto;
import com.stw.dto.CatalogDto.VehicleDto;
import com.stw.entities.CharacterEntity;
import com.stw.entities.FilmEntity;
import com.stw.entities.NamedResource;
import com.stw.repository.CharacterRepository;
import com.stw.repository.FilmRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;


@Service 
@Transactional(readOnly = true)
public class CatalogService {
    private final CharacterRepository characters;
    private final FilmRepository films;
    
    public CatalogService(CharacterRepository characters, FilmRepository films) { 
        this.characters = characters;
        this.films = films;
    }

    public PageDto<CharacterDto> characters(String name, int page, int size, String sort) {
        return PageDto.from(
            characters.findAll(named(name), 
            pageable(page, size, sort))
            .map(CharacterDto::from));
    }

    public CharacterDto character(long id) { 
        CharacterEntity person = characters.findById(id).orElseThrow(ApiException::notFound);
        return CharacterDto.from(person); 
    }

    public PageDto<FilmDto> films(String name, int page, int size, String sort) {
        return PageDto.from(
            films.findAll(named(name), pageable(page, size, sort))
            .map(FilmDto::from));
    }

    public List<FilmDto> characterFilms(long id) { 
        CharacterEntity character = characters.findById(id).orElseThrow(ApiException::notFound);
        return map(character.films, FilmDto::from); 
    }

    public FilmDto film(long id) { 
        FilmEntity film = films.findById(id).orElseThrow(ApiException::notFound);
        return FilmDto.from(film); 
    }

    public List<VehicleDto> characterVehicles(long id) {
        CharacterEntity person = characters.findById(id).orElseThrow(ApiException::notFound);
        return map(person.vehicles, VehicleDto::from); 
    }

    public List<StarshipDto> characterStarships(long id) { 
        CharacterEntity person = characters.findById(id).orElseThrow(ApiException::notFound);
        return map(person.starships, StarshipDto::from); 
    }

    public List<CharacterDto> filmCharacters(long id) { 
        FilmEntity film = films.findById(id).orElseThrow(ApiException::notFound);
        return map(film.characters, CharacterDto::from); 
    }

    public List<VehicleDto> filmVehicles(long id) { 
        FilmEntity film = films.findById(id).orElseThrow(ApiException::notFound);
        return map(film.vehicles, VehicleDto::from); 
    }

    private <T> Specification<T> named(String name) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                
                Expression<String> columName = root.get("name");
                
                Expression<String> minusColumn = cb.lower(columName);
                
                String scapeString = escape(name);
                String patterSearch = "%" + scapeString + "%";
                
                char characterScape = '\\';
                
                Predicate conditionLike = cb.like(minusColumn, patterSearch, characterScape);
 
                return conditionLike;
            }
        };
    }

    private <E extends NamedResource, D> List<D> map(Set<E> entities, Function<E, D> mapper) {
        List<E> sortedEntities = new ArrayList<>(entities);
        
        sortedEntities.sort(Comparator.comparing(e -> e.id));
        
        List<D> resultList = new ArrayList<>();
        
        for (E entity : sortedEntities) {
            D dto = mapper.apply(entity);
            resultList.add(dto);
        }
        
        return resultList;
    }

    private String escape(String value) { 
        return value.toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_"); 
    }

    private Pageable pageable(int page, int size, String sort) {
        String[] parts = sort.split(",", -1);
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("El parámetro de ordenamiento debe tener el formato 'campo,dirección'");
        }
        
        String field = parts[0];
        String direction = parts[1];

        if (page < 0) {
            throw new IllegalArgumentException("El número de página no puede ser menor a 0");
        }
        
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("El tamaño de la página debe estar entre 1 y 100 elementos");
        }

        Set<String> fieldPermitted = Set.of("id", "name");
        if (!fieldPermitted.contains(field)) {
            throw new IllegalArgumentException("No se permite ordenar por el campo: " + field);
        }

        Set<String> directionPermitted = Set.of("asc", "desc");
        if (!directionPermitted.contains(direction)) {
            throw new IllegalArgumentException("Dirección de ordenamiento inválida: " + direction);
        }

        Sort.Direction directionEnum = Sort.Direction.fromString(direction);
        Sort ordering = Sort.by(directionEnum, field);


        if (!field.equals("id")) {
            ordering = ordering.and(Sort.by("id"));
        }

        return PageRequest.of(page, size, ordering);
    }

}
