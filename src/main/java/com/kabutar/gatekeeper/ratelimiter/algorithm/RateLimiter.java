package com.kabutar.gatekeeper.ratelimiter.algorithm;

import org.springframework.web.server.ServerWebExchange;

public interface RateLimiter {

    /**
     * checks if the request is rate limited
     * @param exchange
     * @return
     */
    boolean isRateLimited(ServerWebExchange exchange);

    /**
     * defines how rate limited requests will be handled
     * @param exchange
     */
    void handleRateLimited(ServerWebExchange exchange);
}
