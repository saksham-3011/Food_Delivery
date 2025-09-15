package com.food.delivery.customer_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is a simple data class (POJO) to hold customer information.
 * @Data - A Lombok annotation to create getters, setters, toString, etc.
 * @AllArgsConstructor - A Lombok annotation to create a constructor with all arguments.
 * @NoArgsConstructor - A Lombok annotation to create an empty constructor.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    private String name;
    private String address;
}