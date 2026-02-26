package com.kabutar.gatekeeper.ratelimiter.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class DropRequestHandler implements RateLimitedHandler{
    @Override
    public void handle(ServerWebExchange exchange) {

    }
}
