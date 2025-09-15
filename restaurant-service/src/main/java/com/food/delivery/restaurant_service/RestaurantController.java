package com.food.delivery.restaurant_service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Using Java Records for concise data classes
record ReservationRequest(List<String> items) {}
record ReleaseRequest(String reservationId) {}
record ReservationResponse(String reservationId) {}
record RestaurantInfo(String name) {}

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {
    
    // Simple in-memory data
    private final Map<String, RestaurantInfo> restaurants = Map.of(
        "res1", new RestaurantInfo("Pizza Palace"),
        "res2", new RestaurantInfo("Burger Barn")
    );

    @GetMapping("/{id}")
    public RestaurantInfo getRestaurantInfo(@PathVariable String id) {
        System.out.println("[Restaurant Service] Fetched restaurant " + id);
        return restaurants.get(id);
    }

    // This is a step in the Saga
    @PostMapping("/{id}/reserve")
    public ReservationResponse reserveFood(@PathVariable String id, @RequestBody ReservationRequest request) {
        String reservationId = "resv-" + System.currentTimeMillis();
        System.out.println("[Restaurant Service] Food reserved for " + id + " with ID: " + reservationId);
        return new ReservationResponse(reservationId);
    }

    // This is a compensating action for the Saga
    @PostMapping("/{id}/release")
    public ResponseEntity<String> releaseFood(@PathVariable String id, @RequestBody ReleaseRequest request) {
        System.out.println("[Restaurant Service]  compensating: Releasing food reservation ID: " + request.reservationId());
        return ResponseEntity.ok("Food reservation released");
    }
}
