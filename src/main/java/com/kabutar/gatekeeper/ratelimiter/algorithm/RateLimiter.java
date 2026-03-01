package com.kabutar.gatekeeper.ratelimiter.algorithm;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface RateLimiter {

    /**
     * checks if the request is rate limited
     * @param exchange
     * @return
     */
    Mono<Void> allocate(ServerWebExchange exchange, GatewayFilterChain chain);
}
