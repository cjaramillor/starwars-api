package com.stw.service;

import tools.jackson.databind.JsonNode;
import com.stw.commons.ApiException;
import com.stw.client.SwapiFeignClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.net.URI;


@Service
public class SwapiService {

    private static final Logger log = LoggerFactory.getLogger(SwapiService.class);

    private final URI base;
    private final SwapiFeignClient feignClient;

    public SwapiService(@Value("${app.swapi.base-url}") String baseUrl,
                        SwapiFeignClient feignClient) {
        this.feignClient = feignClient;
        URI configuredBase;
        try {
            configuredBase = URI.create(baseUrl);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw invalidConfiguration();
        }
        if (!configuredBase.isAbsolute() || configuredBase.getHost() == null
                || !("https".equalsIgnoreCase(configuredBase.getScheme())
                || "http".equalsIgnoreCase(configuredBase.getScheme()))
                || configuredBase.getUserInfo() != null || configuredBase.getQuery() != null
                || configuredBase.getFragment() != null || configuredBase.getRawPath().contains("%")) {
            throw invalidConfiguration();
        }
        String url = configuredBase.toString();
        this.base = URI.create(url.endsWith("/") ? url : url + "/").normalize();
    }

    private ApiException invalidConfiguration() {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                "SWAPI_BASE_URL inválida en la configuración");
    }

    public JsonNode get(String url) {
        URI validatedUri = validate(url);
        JsonNode body;
        try {
            body = feignClient.getAbsoluteUrl(validatedUri);
        } catch (FeignException ex) {
            log.error("Error al consultar SWAPI en {}: estado {}", validatedUri, ex.status());
            throw upstream("Error de comunicación o respuesta no interpretable de SWAPI");
        }
        if (body == null || !body.isObject()) {
            throw upstream("Respuesta SWAPI vacía o inválida");
        }
        return body;
    }
    public String peopleUrl() {
        return base.resolve("people/").toString();
    }

    public URI validate(String url) {
        URI uri;
        try {
            uri = base.resolve(url).normalize();
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw upstream("URL SWAPI inválida");
        }

        if (!base.getScheme().equalsIgnoreCase(uri.getScheme()) 
                || !base.getHost().equalsIgnoreCase(uri.getHost())
                || base.getPort() != uri.getPort() 
                || uri.getUserInfo() != null 
                || uri.getFragment() != null
                || !uri.getPath().startsWith(base.getPath()) 
                || uri.getRawPath().contains("%")) {
            throw upstream("SWAPI devolvió una URL fuera de la fuente configurada");
        }
        return uri;
    }

    public long id(String url, String resource) {
        URI uri = validate(url);
        String prefix = base.getPath() + resource + "/";
        
        if (!uri.getPath().startsWith(prefix) || uri.getQuery() != null) {
            throw upstream("Relación SWAPI inválida");
        }
        
        String value = uri.getPath().substring(prefix.length()).replaceFirst("/$", "");
        try {
            long id = Long.parseLong(value);
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException ignored) {
            // Error de contrato del upstream, capturado abajo
        }
        throw upstream("ID SWAPI inválido");
    }

    public ApiException upstream(String message) {
        return new ApiException(HttpStatus.BAD_GATEWAY, message);
    }
}

