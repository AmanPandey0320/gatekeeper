package com.kabutar.gatekeeper.ratelimiter.handler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(prefix = "config.rate-limited", name = "strategy", havingValue = "drop")
public class DropRequestHandler implements RateLimitedHandler{
    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return response.setComplete();
    }
}
