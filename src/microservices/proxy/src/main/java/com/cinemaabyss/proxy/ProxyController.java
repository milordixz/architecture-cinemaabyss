package com.cinemaabyss.proxy;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@EnableConfigurationProperties(ProxyProperties.class)
public class ProxyController {
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "transfer-encoding", "upgrade", "host", "content-length");

    private final RestClient restClient;
    private final ProxyProperties properties;

    public ProxyController(RestClient.Builder builder, ProxyProperties properties) {
        restClient = builder.build();
        this.properties = properties;
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Strangler Fig Proxy is healthy");
    }

    @RequestMapping(value = "/**", method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH,
            RequestMethod.DELETE, RequestMethod.HEAD, RequestMethod.OPTIONS
    })
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        String path = request.getRequestURI();
        URI target = URI.create(stripTrailingSlash(selectTarget(path)) + path
                + (request.getQueryString() == null ? "" : "?" + request.getQueryString()));

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                Enumeration<String> values = request.getHeaders(name);
                while (values.hasMoreElements()) headers.add(name, values.nextElement());
            }
        }

        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
        return restClient.method(HttpMethod.valueOf(request.getMethod()))
                .uri(target)
                .headers(targetHeaders -> targetHeaders.putAll(headers))
                .body(body)
                .exchange((requestSpec, response) -> {
                    HttpHeaders responseHeaders = new HttpHeaders();
                    response.getHeaders().forEach((name, values) -> {
                        if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                            responseHeaders.put(name, values);
                        }
                    });
                    byte[] responseBody = response.getBody() == null
                            ? new byte[0] : response.getBody().readAllBytes();
                    return ResponseEntity.status(response.getStatusCode())
                            .headers(responseHeaders)
                            .body(responseBody);
                });
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<String> upstreamUnavailable(ResourceAccessException exception) {
        return ResponseEntity.status(502).body("Upstream service unavailable");
    }

    private String selectTarget(String path) {
        if (path.startsWith("/api/events")) {
            return properties.getEventsServiceUrl();
        }
        if (path.startsWith("/api/movies") && useMoviesService()) {
            return properties.getMoviesServiceUrl();
        }
        return properties.getMonolithUrl();
    }

    private boolean useMoviesService() {
        return !properties.isGradualMigration()
                || ThreadLocalRandom.current().nextInt(100) < properties.getMoviesMigrationPercent();
    }

    private static String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
