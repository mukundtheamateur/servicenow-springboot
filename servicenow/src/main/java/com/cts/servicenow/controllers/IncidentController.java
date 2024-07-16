package com.cts.servicenow.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cts.servicenow.services.IncidentService;



@RestController
public class IncidentController {
    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping("/incidents")
    public ResponseEntity<List<Map<String, Object>>> getIncidents() {
        return incidentService.getIncidents();
    }
}

