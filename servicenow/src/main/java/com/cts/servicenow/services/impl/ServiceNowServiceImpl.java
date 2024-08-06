package com.cts.servicenow.services.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cts.servicenow.config.ServiceNowConfig;
import com.cts.servicenow.entity.AssignedTo;
import com.cts.servicenow.entity.Incident;
import com.cts.servicenow.services.ServiceNowService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ServiceNowServiceImpl implements ServiceNowService {

    private final RestTemplate restTemplate;
    private final ServiceNowConfig serviceNowConfig;

    @Autowired
    public ServiceNowServiceImpl(RestTemplate restTemplate, ServiceNowConfig serviceNowConfig) {
        this.restTemplate = restTemplate;
        this.serviceNowConfig = serviceNowConfig;
    }

    @Override
    public List<Incident> fetchNewIncidents() {
        String url = serviceNowConfig.getInstanceUrl() + "/api/now/table/incident?sysparm_query=ORDERBYDESCsys_created_on";
        HttpHeaders headers = createHeaders(serviceNowConfig.getUsername(), serviceNowConfig.getPassword());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return parseIncidents(response.getBody());
        } else {
            log.error("Failed to fetch incidents from ServiceNow: " + response.getStatusCode());
            return Collections.emptyList();
        }
    }

    public HttpHeaders createHeaders(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("Accept", "application/json");
        return headers;
    }

    private List<Incident> parseIncidents(String responseBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(responseBody);
            JsonNode results = root.path("result");

            List<Incident> incidents = new ArrayList<>();
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
            return incidents;
        } catch (Exception e) {
            log.error("Error while parsing incidents from ServiceNow response", e);
            return Collections.emptyList();
        }
    }
}
