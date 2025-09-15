package com.food.delivery.api_gateway;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/orders/summary")
public class OrderSummaryController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${order.service.url}")
    private String orderServiceUrl;
    @Value("${customer.service.url}")
    private String customerServiceUrl;
    @Value("${restaurant.service.url}")
    private String restaurantServiceUrl;
    @Value("${delivery.service.url}")
    private String deliveryServiceUrl;

    @GetMapping("/{id}")
    public Map<String, Object> getOrderSummary(@PathVariable String id) {
        System.out.println("[API Gateway] Aggregating data for order summary " + id);

        // 1. Get base order data from Order Service
        Map<String, Object> orderData = restTemplate.getForObject(orderServiceUrl + "/orders/" + id, Map.class);
        Objects.requireNonNull(orderData, "Order data could not be fetched.");

        if (!"SUCCESS".equals(orderData.get("status"))) {
            return Map.of("orderId", id, "status", orderData.get("status"), "message", "Order was not successful.");
        }

        // 2. Get enrichment data from other services
        Map<?, ?> customerData = restTemplate.getForObject(customerServiceUrl + "/customers/" + orderData.get("customerId"), Map.class);
        Map<?, ?> restaurantData = restTemplate.getForObject(restaurantServiceUrl + "/restaurants/" + orderData.get("restaurantId"), Map.class);
        Map<?, ?> deliveryData = restTemplate.getForObject(deliveryServiceUrl + "/delivery/" + orderData.get("partnerId"), Map.class);

        // 3. Combine (aggregate) all the data into a single response
        return Map.of(
            "orderId", id,
            "status", orderData.get("status"),
            "customer", customerData,
            "restaurant", restaurantData,
            "deliveryPartner", deliveryData,
            "items", orderData.get("items")
        );
    }
}