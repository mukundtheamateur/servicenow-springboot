package com.cts.servicenow.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.cts.servicenow.exceptions.ServiceNowException;

import lombok.Data;

@Component
@Data
public class ServiceNowClient {
    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;
    private final String instanceUrl;
    private final String username;
    private final String password;

    public ServiceNowClient(@Value("${servicenow.instance-url}") String instanceUrl,
                            @Value("${servicenow.username}") String username,
                            @Value("${servicenow.password}") String password,
                            RestTemplate restTemplate) {
        this.baseUrl = instanceUrl + "/api/now/table/incident";
        this.restTemplate = restTemplate;
        this.instanceUrl = instanceUrl;
        this.headers = new HttpHeaders();
        this.username = username;
        this.password = password;
        headers.setBasicAuth(username, password);
    }

    public ResponseEntity<String> getIncidents() {
        try {
            HttpEntity<String> request = new HttpEntity<>(headers);
            return restTemplate.exchange(baseUrl + "?sysparm_query=ORDERBYDESCsys_created_on", HttpMethod.GET, request, String.class);
        } catch (Exception e) {
            throw new ServiceNowException("Error while fetching incidents from ServiceNow");
        }
    }
    

}
