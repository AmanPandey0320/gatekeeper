package com.kabutar.gatekeeper.ratelimiter.algorithm;

import com.kabutar.gatekeeper.config.rateLimit.FixedWindowConfig;
import com.kabutar.gatekeeper.config.rateLimit.Rule;
import com.kabutar.gatekeeper.ratelimiter.handler.RateLimitedHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class FixedWindowCounterRatelimiter implements RateLimiter{
    private Logger logger = LogManager.getLogger(FixedWindowCounterRatelimiter.class);
    private RateLimitedHandler handler;
    private FixedWindowConfig config;

    public FixedWindowCounterRatelimiter(RateLimitedHandler handler, Rule rule){
        this.handler = handler;
        this.config = rule.getConfig().getFixedWindow();
    }
    @Override
    public Mono<Void> allocate(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.debug("Entering allocate method of {} for path: {}",FixedWindowCounterRatelimiter.class.getCanonicalName(),exchange.getRequest().getPath());
        //TODO: implement here
        logger.debug("Exiting allocate method of {} for path: {}",FixedWindowCounterRatelimiter.class.getCanonicalName(),exchange.getRequest().getPath());
        return chain.filter(exchange);
    }
}
