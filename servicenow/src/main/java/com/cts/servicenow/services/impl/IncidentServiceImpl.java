package com.cts.servicenow.services.impl;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cts.servicenow.client.ServiceNowClient;
import com.cts.servicenow.entity.AssignedTo;
import com.cts.servicenow.entity.Incident;
import com.cts.servicenow.entity.ServiceNowResponse;
import com.cts.servicenow.exceptions.ServiceNowException;
import com.cts.servicenow.services.IncidentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IncidentServiceImpl implements IncidentService{

    private final ServiceNowClient serviceNowClient;
    private final RestTemplate restTemplate;

    @Autowired
    public IncidentServiceImpl(ServiceNowClient serviceNowClient, RestTemplate restTemplate) {
        this.serviceNowClient = serviceNowClient;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<List<Incident>> getIncidents() {
        ResponseEntity<String> response = serviceNowClient.getIncidents();
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Convert JSON string to JsonNode
            JsonNode root = mapper.readTree(response.getBody());
            // Get "result" array
            JsonNode results = root.path("result");

            List<Incident> incidents = new ArrayList<>();
            // Loop over all incidents in the result array
            for (JsonNode result : results) {
                Incident incident = new Incident();
                incident.setId(result.path("sys_id").asText());
                incident.setIncidentNumber(result.path("number").asText());
                incident.setStatus(result.path("state").asText());
                incident.setOpenedAt(result.path("opened_at").asText());
                incident.setCreatedBy(result.path("sys_created_by").asText());
                incident.setDescription(result.path("description").asText());
                incident.setSlaDue(result.path("sla_due").asText());
                // Handle assigned_to field
                JsonNode assignedToNode = result.path("assigned_to");
                if (assignedToNode.isObject()) {
                    AssignedTo assignedTo = mapper.treeToValue(assignedToNode, AssignedTo.class);
                    incident.setAssignedTo(assignedTo);
                } else {
                    incident.setAssignedTo(new AssignedTo(assignedToNode.asText()));
                }

                incidents.add(incident);
            }

            log.info(incidents.toString());
            return ResponseEntity.ok(incidents);
        } catch (Exception e) {
            throw new ServiceNowException("Error while fetching incidents from ServiceNow");
        }
    }
    
    public Incident getLatestOpenedIncident() {
        String instanceUrl = serviceNowClient.getInstanceUrl();
        String username = serviceNowClient.getUsername();
        String password = serviceNowClient.getPassword();

        if (instanceUrl == null || username == null || password == null) {
            throw new IllegalArgumentException("ServiceNow client configuration is missing");
        }

        String url = instanceUrl + "/api/now/table/incident?sysparm_query=active=true^ORDERBYDESCopened_at&sysparm_limit=1";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ServiceNowResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, ServiceNowResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().getResult().isEmpty()) {
                return response.getBody().getResult().get(0);
            } else {
                throw new RuntimeException("Failed to fetch incidents from ServiceNow");
            }
        } catch (Exception e) {
            log.error("Exception occurred while fetching incidents from ServiceNow", e);
            throw new RuntimeException("Failed to fetch incidents from ServiceNow", e);
        }
    }
    
    public List<Incident> getAllOpenedIncidents() {
        String url = serviceNowClient.getInstanceUrl() + "/api/now/table/incident?sysparm_query=active=true^ORDERBYDESCopened_at";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(serviceNowClient.getUsername(), serviceNowClient.getPassword());
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ServiceNowResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, ServiceNowResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().getResult();
        } else {
            throw new RuntimeException("Failed to fetch incidents from ServiceNow");
        }
    }
}
