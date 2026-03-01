package com.kabutar.gatekeeper.filter;

import com.kabutar.gatekeeper.ratelimiter.algorithm.RateLimiter;
import com.kabutar.gatekeeper.ratelimiter.factory.RateLimiterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(prefix = "config.rate-limited", name = "enabled", havingValue = "true")
public class RateLimiterGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    RateLimiterFactory rateLimiterFactory;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RateLimiter rateLimiter = rateLimiterFactory.get(exchange);
        return rateLimiter.allocate(exchange,chain);
    }

    @Override
    public int getOrder() {
        return PreGatewayFilterOrder.RATE_LIMITED_FILTER_ORDER;
    }
}
