package com.example.banking.gateway.controller;

import com.example.banking.gateway.config.GatewayProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequiredArgsConstructor
public class ProxyController {

    private final RestClient restClient;
    private final GatewayProperties properties;

    @RequestMapping({"/api/auth/**", "/api/admin/users/**"})
    public ResponseEntity<byte[]> identity(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(properties.services().identityUrl(), request, body);
    }

    @RequestMapping("/api/profiles/**")
    public ResponseEntity<byte[]> profile(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(properties.services().profileUrl(), request, body);
    }

    @RequestMapping("/api/accounts/**")
    public ResponseEntity<byte[]> account(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(properties.services().accountUrl(), request, body);
    }

    @RequestMapping("/api/payments/**")
    public ResponseEntity<byte[]> payment(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(properties.services().paymentUrl(), request, body);
    }

    @RequestMapping("/api/notifications/**")
    public ResponseEntity<byte[]> notification(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(properties.services().notificationUrl(), request, body);
    }

    private ResponseEntity<byte[]> forward(String baseUrl, HttpServletRequest request, byte[] body) {
        String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        URI uri = URI.create(baseUrl + request.getRequestURI() + query);
        RestClient.RequestBodySpec spec = restClient.method(HttpMethod.valueOf(request.getMethod())).uri(uri)
                .headers(headers -> copyHeaders(request, headers));
        ResponseEntity<byte[]> response = body == null || body.length == 0
                ? spec.retrieve().toEntity(byte[].class)
                : spec.body(body).retrieve().toEntity(byte[].class);
        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }

    private void copyHeaders(HttpServletRequest request, HttpHeaders target) {
        Collections.list(request.getHeaderNames()).forEach(name -> {
            if (!name.equalsIgnoreCase(HttpHeaders.HOST) && !name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                target.put(name, Collections.list(request.getHeaders(name)));
            }
        });
    }
}
