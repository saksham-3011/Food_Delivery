package com.food.delivery.order_service;

import java.util.List;

// Using Java Records for concise data classes
record PaymentDetails(String cardNumber) {}

record OrderRequest(String customerId, String restaurantId, List<String> items, PaymentDetails paymentDetails) {}

record Order(String customerId, String restaurantId, List<String> items, String status, String partnerId) {}
