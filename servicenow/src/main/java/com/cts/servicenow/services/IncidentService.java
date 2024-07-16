package com.cts.servicenow.services;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cts.servicenow.client.ServiceNowClient;
import com.cts.servicenow.exceptions.ServiceNowException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IncidentService {
    private final ServiceNowClient serviceNowClient;

    public IncidentService(ServiceNowClient serviceNowClient) {
        this.serviceNowClient = serviceNowClient;
    }

    public ResponseEntity<List<Map<String, Object>>> getIncidents() {
        ResponseEntity<String> response = serviceNowClient.getIncidents();
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Convert JSON string to JsonNode
            JsonNode root = mapper.readTree(response.getBody());
            // Get "result" array
            JsonNode results = root.path("result");

            List<Map<String, Object>> incidents = new ArrayList<>();
            // Loop over all incidents in the result array
            for (JsonNode result : results) {
                Map<String, Object> necessaryDetails = new HashMap<>();
                necessaryDetails.put("incidentNumber", result.path("number").asText());
                necessaryDetails.put("status", result.path("state").asText());
                necessaryDetails.put("openedAt", result.path("opened_at").asText());
                necessaryDetails.put("createdBy", result.path("sys_created_by").path("value").asText());
                necessaryDetails.put("description", result.path("description").asText());
                necessaryDetails.put("assignedTo", result.path("assigned_to").path("value").asText());
                necessaryDetails.put("slaDue", result.path("sla_due").asText());
                incidents.add(necessaryDetails);
            }

            log.info(incidents.toString());
            return ResponseEntity.ok(incidents);
        } catch (Exception e) {
            throw new ServiceNowException("Error while fetching incidents from ServiceNow", e);
        }
    }


}

