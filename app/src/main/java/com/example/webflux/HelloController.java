package com.example.webflux;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final String externalApiUrl;

    public HelloController(@Value("${external.api.url:https://postman-echo.com/get?foo=bar}") String externalApiUrl) {
        this.externalApiUrl = externalApiUrl;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        try {
            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain;charset=UTF-8")
                    .body(fetchExternalMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .header("Content-Type", "text/plain;charset=UTF-8")
                    .body("外部API呼び出しに失敗しました");
        }
    }

    @GetMapping("/hello/{id}")
    public ResponseEntity<String> helloById(@PathVariable String id) {
        return ResponseEntity.ok()
                .header("Content-Type", "text/plain;charset=UTF-8")
                .body("hello-" + id);
    }

    @GetMapping("/hello/admin/{id}")
    public ResponseEntity<String> helloAdminById(@PathVariable String id) {
        return ResponseEntity.ok()
                .header("Content-Type", "text/plain;charset=UTF-8")
                .body("admin-hello-" + id);
    }

    private String fetchExternalMessage() throws IOException {
        Request externalRequest = new Request.Builder().url(externalApiUrl).build();
        try (Response response = okHttpClient.newCall(externalRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("External API returned status " + response.code());
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IllegalStateException("External API response body is empty");
            }
            return body.string();
        }
    }
}
