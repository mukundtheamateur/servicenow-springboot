package com.cts.servicenow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.cts.servicenow.client.ServiceNowClient;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@Data
public class ServiceNowConfig {
	
    private String instanceUrl;
    private String username;
    private String password;
	
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ServiceNowClient serviceNowClient(@Value("${servicenow.instance-url}") String instanceUrl,
                                             @Value("${servicenow.username}") String username,
                                             @Value("${servicenow.password}") String password) {
    	
    	log.info("########service now login success###########");
        return new ServiceNowClient(instanceUrl, username, password, restTemplate());
    }
}

