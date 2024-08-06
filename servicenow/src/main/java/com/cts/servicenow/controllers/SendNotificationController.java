package com.cts.servicenow.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.servicenow.entity.Incident;
import com.cts.servicenow.services.SendNotification;
import com.cts.servicenow.services.impl.IncidentServiceImpl;

@RestController
@RequestMapping("/api")
public class SendNotificationController {

    private final SendNotification notification;
    private final IncidentServiceImpl incidentService;

    @Autowired
    public SendNotificationController(SendNotification notification, IncidentServiceImpl incidentService) {
        this.notification = notification;
        this.incidentService = incidentService;
    }

    @PostMapping("/all-incidents")
    public ResponseEntity<String> sendNotification() {
        // Fetch new incidents from ServiceNow
        List<Incident> newIncidents = incidentService.getIncidents().getBody();

        // Send notifications for each new incident
        if (newIncidents != null) {
            for (Incident incident : newIncidents) {
                String message = String.format("New Incident: %s - %s", incident.getId(), incident.getDescription());
                notification.sendDiscordNotification(message);
            }
        }

        return ResponseEntity.ok("Notifications sent for new incidents.");
    }
    
    @PostMapping("/latest-opened")
    public ResponseEntity<String> getLatestOpenedIncidentAndNotify() {
        Incident incident = incidentService.getLatestOpenedIncident();
        if (incident != null) {
            // Prepare the message
            String message = String.format("-----NEW MESSAGE-----\nLATEST OPENED INCIDENT ⚠️: \n %s \n %s \n Opened at- %s \n Assigned to - %s", incident.getIncidentNumber(), incident.getDescription(), incident.getOpenedAt(), incident.getAssignedTo());

            // Send the notification to Discord
            String notificationResponse = notification.sendDiscordNotification(message);

            // Return the response
            return ResponseEntity.ok("Incident fetched and notification sent: " + notificationResponse);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    
    @PostMapping("/all-opened")
    public ResponseEntity<String> getAllOpenedIncidentsAndNotify() {
        List<Incident> incidents = incidentService.getAllOpenedIncidents();
        if (incidents != null && !incidents.isEmpty()) {
            StringBuilder notificationResponse = new StringBuilder();
            for (Incident incident : incidents) {
                // Prepare the message
                String message = String.format("-----NEW OPENED INCIDENT-----\nOPENED INCIDENT ⚠️: \n %s \n %s \n Opened at- %s \n %s", 
                                                incident.getIncidentNumber(), 
                                                incident.getDescription(), 
                                                incident.getOpenedAt(), 
                                                incident.getAssignedTo());

                // Send the notification to Discord
                String response = notification.sendDiscordNotification(message);
                notificationResponse.append("Incident ").append(incident.getIncidentNumber()).append(": ").append(response).append("\n");
            }

            // Return the response
            return ResponseEntity.ok("Incidents fetched and notifications sent:\n" + notificationResponse.toString());
        } else {
            return ResponseEntity.noContent().build();
        }
    }

}
