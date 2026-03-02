package com.kabutar.gatekeeper.ratelimiter.algorithm;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class FixedWindowCounterRatelimiter implements RateLimiter{
    @Override
    public Mono<Void> allocate(ServerWebExchange exchange, GatewayFilterChain chain) {
        return null;
    }
}
