package com.food.delivery.customer_service;

// --- ADD THESE IMPORTS ---
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
// -------------------------

@RestController
@RequestMapping("/customers")
public class CustomerController {

    // This map acts as a simple in-memory database for the assignment.
    private final Map<String, Customer> customers = new ConcurrentHashMap<>(Map.of(
        "123", new Customer("John Doe", "123 Maple Street, Anytown"),
        "456", new Customer("Jane Smith", "456 Oak Avenue, Sometown")
    ));

    /**
     * Handles GET requests to find a customer by their ID.
     * Example URL: /customers/123
     * @param id The customer's ID from the URL path.
     * @return The Customer object.
     */
    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable String id) {
        System.out.println("[Customer Service] Fetched customer " + id);
        return customers.get(id);
    }
}