package com.cts.servicenow.client;

import org.springframework.web.client.RestTemplate;

import com.cts.servicenow.exceptions.ServiceNowException;

import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ServiceNowClient {
    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;

    public ServiceNowClient(@Value("${servicenow.instance-url}") String instanceUrl,
                            @Value("${servicenow.username}") String username,
                            @Value("${servicenow.password}") String password) {
        this.baseUrl = instanceUrl + "/api/now/table/incident";
        this.restTemplate = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
    }

    public ResponseEntity<String> getIncidents() {
        try {
            HttpEntity<String> request = new HttpEntity<>(headers);
            return restTemplate.exchange(baseUrl, HttpMethod.GET, request, String.class);
        } catch (Exception e) {
            throw new ServiceNowException("Error while fetching incidents from ServiceNow", e);
        }
    }
    
    
}

