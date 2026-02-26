package com.kabutar.gatekeeper.ratelimiter.handler;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface RateLimitedHandler {
    Mono<Void> handle(ServerWebExchange exchange);
}
