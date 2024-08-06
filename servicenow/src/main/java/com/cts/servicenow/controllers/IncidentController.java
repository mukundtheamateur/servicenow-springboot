package com.cts.servicenow.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.servicenow.entity.Incident;
import com.cts.servicenow.services.IncidentService;
import com.cts.servicenow.services.impl.IncidentServiceImpl;



@RestController
public class IncidentController {
	
    private final IncidentService incidentService;

    public IncidentController(IncidentServiceImpl incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping("/incidents")
    public ResponseEntity<List<Incident>> getIncidents() {
        return incidentService.getIncidents();
    }
    
    @GetMapping("/latest-opened")
    public ResponseEntity<Incident> getLatestOpenedIncident() {
        Incident incident = incidentService.getLatestOpenedIncident();
        if (incident != null) {
            return ResponseEntity.ok(incident);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}

