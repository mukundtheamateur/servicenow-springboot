package com.cts.servicenow.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Incident {
    private String id;

    @JsonProperty("number")
    private String incidentNumber;

    @JsonProperty("state")
    private String status;

    @JsonProperty("opened_at")
    private String openedAt;

    @JsonProperty("sys_created_by")
    private String createdBy;

    @JsonProperty("short_description")
    private String description;

    @JsonProperty("assigned_to")
    private AssignedTo assignedTo;

    @JsonProperty("due_date")
    private String slaDue;

    @JsonProperty("sys_id")
    private String sysId;

    private boolean active;
}

