package com.cts.servicenow.entity;

import java.util.List;

public class ServiceNowResponse {
    private List<Incident> result;

    public List<Incident> getResult() {
        return result;
    }

    public void setResult(List<Incident> result) {
        this.result = result;
    }
}

