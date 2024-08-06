package com.cts.servicenow.services;

import java.util.List;

import org.springframework.http.HttpHeaders;

import com.cts.servicenow.entity.Incident;

public interface ServiceNowService {

	List<Incident> fetchNewIncidents();
	HttpHeaders createHeaders(String username, String password);
}
