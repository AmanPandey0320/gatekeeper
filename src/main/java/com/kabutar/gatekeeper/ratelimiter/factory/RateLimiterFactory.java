package com.kabutar.gatekeeper.ratelimiter.factory;

import com.kabutar.gatekeeper.ratelimiter.algorithm.RateLimiter;
import org.springframework.web.server.ServerWebExchange;

public interface RateLimiterFactory {
    RateLimiter get();
    RateLimiter get(ServerWebExchange exchange);
    RateLimiter init(String algorithm);
    RateLimiter init(String algorithm, Object object);
}
