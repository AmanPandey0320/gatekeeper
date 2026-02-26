package com.kabutar.gatekeeper.ratelimiter.handler;

import org.springframework.web.server.ServerWebExchange;

public interface RateLimitedHandler {
    void handle(ServerWebExchange exchange);
}
