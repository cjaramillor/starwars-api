package com.stw.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.stw.service.SwapiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.net.URI;

@Component
public class SwapiClient {

    private static final Logger log = LoggerFactory.getLogger(SwapiClient.class);
    
    private final SwapiFeignClient feignClient;
    private final SwapiService swapiService;

    public SwapiClient(SwapiFeignClient feignClient, SwapiService swapiService) {
        this.feignClient = feignClient;
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
        URI uriValida = swapiService.validate(url);
        
        try {
            JsonNode body = feignClient.getAbsoluteUrl(uriValida);
            
            if (body == null || !body.isObject()) {
                throw swapiService.upstream("Respuesta SWAPI vacía o inválida");
            }
            return body;
            
        } catch (Exception ex) {
            log.error("Error al consultar SWAPI vía Feign: {}", ex.getMessage());
            throw swapiService.upstream("Error de comunicación o respuesta no interpretable de SWAPI");
        }
    }
}
