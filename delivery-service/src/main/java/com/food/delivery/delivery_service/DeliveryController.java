package com.food.delivery.delivery_service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Using Java Records for concise data classes
record AssignmentRequest(String orderId) {}
record CancelRequest(String orderId, String partnerId) {}
record AssignmentResponse(String partnerId) {}
record PartnerInfo(String name, String vehicle) {}

@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    @GetMapping("/{partnerId}")
    public PartnerInfo getPartnerInfo(@PathVariable String partnerId) {
        System.out.println("[Delivery Service] Fetched partner " + partnerId);
        // Returning static data for the example
        return new PartnerInfo("Alex Ray", "Motorcycle");
    }

    // This is a step in the Saga
    @PostMapping("/assign")
    public AssignmentResponse assignPartner(@RequestBody AssignmentRequest request) {
        String partnerId = "partner-" + (int)(Math.random() * 100);
        System.out.println("[Delivery Service] Assigned partner " + partnerId + " for order " + request.orderId());
        return new AssignmentResponse(partnerId);
    }

    // This is a compensating action for the Saga
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelAssignment(@RequestBody CancelRequest request) {
        System.out.println("[Delivery Service]  compensating: Canceled delivery for order " + request.orderId() + " with partner " + request.partnerId());
        return ResponseEntity.ok("Delivery canceled");
    }
}