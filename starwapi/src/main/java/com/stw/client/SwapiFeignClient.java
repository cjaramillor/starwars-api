package com.stw.client;

import tools.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.net.URI;

// El name puede ser cualquier identificador. 
// El url se inyecta dinámicamente desde tus propiedades.
@FeignClient(name = "swapi-client", url = "${app.swapi.base-url}")
public interface SwapiFeignClient {

    // Permite hacer peticiones a una URL absoluta dinámica devuelta por la API
    @GetMapping
    JsonNode getAbsoluteUrl(URI absoluteUri);

    // Endpoint base por si necesitas listar personas directamente
    @GetMapping("/people/")
    JsonNode getPeople();
}
