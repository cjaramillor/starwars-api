package com.stw.client;

import tools.jackson.databind.JsonNode;
import com.stw.service.SwapiService;
import org.springframework.stereotype.Component;
import java.net.URI;

/** Adaptador compatible con los consumidores existentes de SwapiClient. */
@Component
public class SwapiClient {
    private final SwapiService swapiService;

    public SwapiClient(SwapiService swapiService) {
        this.swapiService = swapiService;
    }

    public String peopleUrl() {
        return swapiService.peopleUrl();
    }

    public URI validate(String url) {
        return swapiService.validate(url);
    }

    public long id(String url, String resource) {
        return swapiService.id(url, resource);
    }

    public JsonNode get(String url) {
        return swapiService.get(url);
    }
}