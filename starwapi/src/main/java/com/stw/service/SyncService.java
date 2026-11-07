package com.stw.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.stw.entities.CharacterEntity;
import com.stw.entities.FilmEntity;
import com.stw.entities.VehicleEntity;
import com.stw.entities.StarshipEntity;
import com.stw.repository.CharacterRepository;
import com.stw.repository.FilmRepository;
import com.stw.repository.VehicleRepository;
import com.stw.repository.StarshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SyncService {
    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    public record SyncResult(int characters, int films, int vehicles, int starships, Instant completedAt) {}

    private final SwapiService swapiService;
    private final CharacterRepository characterRepository;
    private final FilmRepository filmRepository;
    private final VehicleRepository vehicleRepository;
    private final StarshipRepository starshipRepository;

    public SyncService(SwapiService swapiService,
                       CharacterRepository characterRepository, FilmRepository filmRepository,
                       VehicleRepository vehicleRepository, StarshipRepository starshipRepository) {
        this.swapiService = swapiService;
        this.characterRepository = characterRepository;
        this.filmRepository = filmRepository;
        this.vehicleRepository = vehicleRepository;
        this.starshipRepository = starshipRepository;
    }

    @Transactional
    public synchronized SyncResult sync() {
        log.info("Iniciando sincronización en base de datos");
        Map<Long, FilmEntity> films = new HashMap<>();
        Map<Long, VehicleEntity> vehicles = new HashMap<>();
        Map<Long, StarshipEntity> starships = new HashMap<>();
        Set<Long> characters = new HashSet<>();
        Set<String> visitedPages = new HashSet<>();
        String nextUrl = swapiService.peopleUrl();

        while (nextUrl != null) {
            String normalizedUrl = swapiService.validate(nextUrl).toString();
            if (!visitedPages.add(normalizedUrl) || visitedPages.size() > 1000) {
                throw swapiService.upstream("Paginación SWAPI cíclica o excesiva");
            }
            JsonNode page = swapiService.get(normalizedUrl);
            if (page == null || !page.path("results").isArray() || !page.has("next")) {
                throw swapiService.upstream("Página SWAPI inválida");
            }
            for (JsonNode data : page.path("results")) {
                long id = swapiService.id(required(data, "url"), "people");
                if (!characters.add(id)) {
                    throw swapiService.upstream("Personaje SWAPI duplicado: " + id);
                }
                CharacterEntity character = characterRepository.findById(id).orElseGet(CharacterEntity::new);
                character.id = id;
                character.name = required(data, "name");
                character.birthYear = required(data, "birth_year");
                character.gender = required(data, "gender");
                character.films.clear();
                for (String url : urls(data, "films")) {
                    character.films.add(syncFilm(url, films, vehicles, starships));
                }
                character.vehicles.clear();
                character.vehicles.addAll(syncVehicles(data, vehicles));
                character.starships.clear();
                character.starships.addAll(syncStarships(data, starships));
                characterRepository.save(character);
            }
            nextUrl = page.get("next").isNull() ? null : required(page, "next");
        }
        SyncResult result = new SyncResult(characters.size(), films.size(), vehicles.size(),
                starships.size(), Instant.now());
        log.info("Sincronización finalizada: {} personajes, {} películas, {} vehículos, {} naves",
                result.characters(), result.films(), result.vehicles(), result.starships());
        return result;
    }

    private FilmEntity syncFilm(String url, Map<Long, FilmEntity> films,
                                Map<Long, VehicleEntity> vehicles, Map<Long, StarshipEntity> starships) {
        long id = swapiService.id(url, "films");
        if (films.containsKey(id)) {
            return films.get(id);
        }
        JsonNode data = swapiService.get(url);
        validateIdConsistency(data, id, "films");
        FilmEntity film = filmRepository.findById(id).orElseGet(FilmEntity::new);
        film.id = id;
        film.name = required(data, "title");
        JsonNode episode = data.path("episode_id");
        if (!episode.isIntegralNumber() || !episode.canConvertToInt() || episode.intValue() < 1) {
            throw swapiService.upstream("Episodio SWAPI inválido");
        }
        film.episodeId = episode.intValue();
        film.director = required(data, "director");
        film.openingCrawl = required(data, "opening_crawl");
        try {
            film.releaseDate = LocalDate.parse(required(data, "release_date"));
        } catch (DateTimeParseException ex) {
            throw swapiService.upstream("Fecha de estreno SWAPI inválida");
        }
        film.vehicles.clear();
        film.vehicles.addAll(syncVehicles(data, vehicles));
        film.starships.clear();
        film.starships.addAll(syncStarships(data, starships));
        FilmEntity saved = filmRepository.save(film);
        films.put(id, saved);
        return saved;
    }

    private List<VehicleEntity> syncVehicles(JsonNode parent, Map<Long, VehicleEntity> cache) {
        List<VehicleEntity> result = new ArrayList<>();
        for (String url : urls(parent, "vehicles")) {
            long id = swapiService.id(url, "vehicles");
            VehicleEntity vehicle = cache.get(id);
            if (vehicle == null) {
                JsonNode data = swapiService.get(url);
                validateIdConsistency(data, id, "vehicles");
                vehicle = vehicleRepository.findById(id).orElseGet(VehicleEntity::new);
                vehicle.id = id;
                vehicle.name = required(data, "name");
                vehicle.model = required(data, "model");
                vehicle.manufacturer = required(data, "manufacturer");
                vehicle.vehicleClass = required(data, "vehicle_class");
                vehicle = vehicleRepository.save(vehicle);
                cache.put(id, vehicle);
            }
            result.add(vehicle);
        }
        return result;
    }

    private List<StarshipEntity> syncStarships(JsonNode parent, Map<Long, StarshipEntity> cache) {
        List<StarshipEntity> result = new ArrayList<>();
        for (String url : urls(parent, "starships")) {
            long id = swapiService.id(url, "starships");
            StarshipEntity starship = cache.get(id);
            if (starship == null) {
                JsonNode data = swapiService.get(url);
                validateIdConsistency(data, id, "starships");
                starship = starshipRepository.findById(id).orElseGet(StarshipEntity::new);
                starship.id = id;
                starship.name = required(data, "name");
                starship.model = required(data, "model");
                starship.manufacturer = required(data, "manufacturer");
                starship.starshipClass = required(data, "starship_class");
                starship = starshipRepository.save(starship);
                cache.put(id, starship);
            }
            result.add(starship);
        }
        return result;
    }

    private void validateIdConsistency(JsonNode data, long expectedId, String resource) {
        if (swapiService.id(required(data, "url"), resource) != expectedId) {
            throw swapiService.upstream("ID SWAPI inconsistente en " + resource);
        }
    }

    private String required(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.path(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw swapiService.upstream("Campo SWAPI inválido o ausente: " + field);
        }
        return value.asText();
    }

    private List<String> urls(JsonNode node, String field) {
        JsonNode array = node.path(field);
        if (!array.isArray()) {
            throw swapiService.upstream("Lista de relaciones SWAPI inválida o ausente: " + field);
        }
        List<String> result = new ArrayList<>();
        for (JsonNode value : array) {
            if (!value.isTextual() || value.asText().isBlank()) {
                throw swapiService.upstream("URL de relación SWAPI inválida en " + field);
            }
            result.add(value.asText());
        }
        return result;
    }
}
