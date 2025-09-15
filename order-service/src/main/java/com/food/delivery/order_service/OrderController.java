package com.food.delivery.order_service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final RestTemplate restTemplate;
    // In-memory store for created orders
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    // Reading service URLs from application.properties
    @Value("${restaurant.service.url}")
    private String restaurantServiceUrl;

    @Value("${delivery.service.url}")
    private String deliveryServiceUrl;

    public OrderController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        String orderId = "ord-" + UUID.randomUUID().toString();
        System.out.println("[Order Service] Received new order " + orderId);

        // This list will hold the "undo" actions for our saga
        List<Runnable> compensationSteps = new ArrayList<>();

        try {
            // --- SAGA STEP 1: Reserve Food ---
            System.out.println("[Order Service] Step 1: Reserving food...");
            Map<String, Object> reserveRequest = Map.of("items", request.items());
            Map<?, ?> reserveResponse = restTemplate.postForObject(restaurantServiceUrl + "/restaurants/" + request.restaurantId() + "/reserve", reserveRequest, Map.class);
            String reservationId = (String) Objects.requireNonNull(reserveResponse).get("reservationId");

            // If this step succeeds, add its "undo" action to our list
            compensationSteps.add(() -> {
                System.out.println("[Order Service] Compensating: Releasing food reservation " + reservationId);
                restTemplate.postForObject(restaurantServiceUrl + "/restaurants/" + request.restaurantId() + "/release", Map.of("reservationId", reservationId), String.class);
            });
            System.out.println("[Order Service] Food reserved successfully.");


            // --- SAGA STEP 2: Process Payment ---
            System.out.println("[Order Service] Step 2: Processing payment...");
            if (request.paymentDetails().cardNumber().endsWith("0")) {
                throw new RuntimeException("Payment failed: Insufficient funds.");
            }
            // Note: Payment has no compensation step in this example, as it's the pivot point.
            System.out.println("[Order Service] Payment successful.");


            // --- SAGA STEP 3: Assign Delivery Partner ---
            System.out.println("[Order Service] Step 3: Assigning delivery partner...");
            Map<String, String> assignRequest = Map.of("orderId", orderId);
            Map<?, ?> assignResponse = restTemplate.postForObject(deliveryServiceUrl + "/delivery/assign", assignRequest, Map.class);
            String partnerId = (String) Objects.requireNonNull(assignResponse).get("partnerId");

            // If this step succeeds, add its "undo" action to our list
            compensationSteps.add(() -> {
                System.out.println("[Order Service] Compensating: Cancelling delivery assignment for partner " + partnerId);
                restTemplate.postForObject(deliveryServiceUrl + "/delivery/cancel", Map.of("orderId", orderId, "partnerId", partnerId), String.class);
            });
            System.out.println("[Order Service] Delivery partner assigned successfully.");

            // --- SAGA SUCCESS ---
            orders.put(orderId, new Order(request.customerId(), request.restaurantId(), request.items(), "SUCCESS", partnerId));
            System.out.println("[Order Service] Order " + orderId + " processed successfully!");
            return ResponseEntity.status(201).body(Map.of("orderId", orderId, "status", "Order created successfully"));

        } catch (Exception e) {
            // --- SAGA FAILURE & COMPENSATION ---
            System.err.println("[Order Service] Saga failed: " + e.getMessage() + ". Starting compensation...");
            
            // Run all "undo" actions in reverse order of how they were added
            Collections.reverse(compensationSteps);
            compensationSteps.forEach(Runnable::run);
            
            orders.put(orderId, new Order(request.customerId(), request.restaurantId(), request.items(), "FAILED", null));
            System.err.println("[Order Service] Order " + orderId + " failed and compensated.");
            return ResponseEntity.status(500).body(Map.of("orderId", orderId, "status", "Order failed", "reason", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable String id) {
        return orders.get(id);
    }
}