package com.cts.servicenow.services;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.cts.servicenow.entity.Incident;

public interface IncidentService {

	ResponseEntity<List<Incident>> getIncidents();
	Incident getLatestOpenedIncident();
	List<Incident> getAllOpenedIncidents();
}
