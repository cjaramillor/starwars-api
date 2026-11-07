package com.stw.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stw.service.SyncService;

@RestController @RequestMapping("/api/admin")
public class SyncController {
    private final SyncService service;

    public SyncController(SyncService service) { 
        this.service = service; 
    }
    @PostMapping("/sync") public SyncService.SyncResult sync() { 
        return service.sync(); 
    }
}

