package com.example.webflux;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class HelloHandler {

    private final WebClient webClient;
    private final String externalApiUrl;

    public HelloHandler(
            WebClient.Builder webClientBuilder,
            @Value("${external.api.url:https://postman-echo.com/get?foo=bar}") String externalApiUrl) {
        this.webClient = webClientBuilder.build();
        this.externalApiUrl = externalApiUrl;
    }

    public Mono<ServerResponse> hello(ServerRequest request) {
        return fetchExternalMessage()
                .flatMap(body -> ServerResponse.ok().bodyValue(body))
                .onErrorResume(ex -> ServerResponse.status(HttpStatus.BAD_GATEWAY)
                        .bodyValue("外部API呼び出しに失敗しました"));
    }

    public Mono<ServerResponse> helloById(ServerRequest request) {
        String id = request.pathVariable("id");
        return ServerResponse.ok().bodyValue("hello-" + id);
    }

    public Mono<ServerResponse> helloAdminById(ServerRequest request) {
        String id = request.pathVariable("id");
        return ServerResponse.ok().bodyValue("admin-hello-" + id);
    }

    private Mono<String> fetchExternalMessage() {
        return webClient.get()
                .uri(externalApiUrl)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.createException())
                .bodyToMono(String.class)
                .switchIfEmpty(Mono.error(new IllegalStateException("External API response body is empty")));
    }
}
