package com.kabutar.gatekeeper.ratelimiter.algorithm;

import com.kabutar.gatekeeper.config.RateLimitedConfig;
import com.kabutar.gatekeeper.ratelimiter.handler.RateLimitedHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(prefix = "config.rate-limited", name = "algorithm", havingValue = "TB")
public class TokenBucketRateLimiter implements RateLimiter{
    private static final Logger logger = LogManager.getLogger(TokenBucketRateLimiter.class);

    @Autowired
    private RateLimitedHandler handler;

    @Autowired
    private RateLimitedConfig config;


    @Override
    public boolean isRateLimited(ServerWebExchange exchange) {
        return false;
    }

    @Override
    public Mono<Void> handleRateLimited(ServerWebExchange exchange) {
        return handler.handle(exchange);
    }
}
