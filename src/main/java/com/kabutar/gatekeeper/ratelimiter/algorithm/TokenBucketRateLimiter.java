package com.kabutar.gatekeeper.ratelimiter.algorithm;

import com.kabutar.gatekeeper.config.RateLimitedConfig;
import com.kabutar.gatekeeper.ratelimiter.handler.RateLimitedHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class TokenBucketRateLimiter implements RateLimiter{
    private static final Logger logger = LogManager.getLogger(TokenBucketRateLimiter.class);

    private RateLimitedHandler handler;

    private RateLimitedConfig.Rule rule;

    public TokenBucketRateLimiter(RateLimitedHandler handler, RateLimitedConfig.Rule rule){
        this.handler = handler;
        this.rule = rule;
    }

    @Override
    public boolean allocate(ServerWebExchange exchange) {
        logger.info(rule.toString());
        return true;
    }

    @Override
    public Mono<Void> handleRateLimited(ServerWebExchange exchange) {
        return handler.handle(exchange);
    }
}
