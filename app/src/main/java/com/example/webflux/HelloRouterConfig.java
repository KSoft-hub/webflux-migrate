package com.example.webflux;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class HelloRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> helloRoute(HelloHandler helloHandler, RequestHeaderFilter requestHeaderFilter) {
        return RouterFunctions.route()
                .filter(requestHeaderFilter)
                .path("/hello", builder -> builder
                        .GET("", helloHandler::hello)
                        .GET("/{id}", helloHandler::helloById)
                        .path("/admin", adminBuilder -> adminBuilder
                                .GET("/{id}", helloHandler::helloAdminById)))
                .build();
    }
}
