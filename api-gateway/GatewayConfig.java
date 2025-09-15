package com.food.delivery.api_gateway;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.rewritePath;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return GatewayRouterFunctions.route("order_service_route")
                .route(request -> request.path().startsWith("/api/orders"), http("http://order-service:8081"))
                .filter(rewritePath("/api/(?<segment>.*)", "/${segment}"))
                .build();
    }
}