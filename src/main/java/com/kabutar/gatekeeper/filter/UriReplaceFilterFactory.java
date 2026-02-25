package com.kabutar.gatekeeper.filter;

import java.net.URI;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UriReplaceFilterFactory implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI originalUri = exchange.getRequest().getURI();
        String rawPath = originalUri.getRawPath();

        // Replace [ and ] with encoded versions
        if (rawPath.contains("[") || rawPath.contains("]")) {
            String encodedPath = rawPath.replace("[", "%5B").replace("]", "%5D");
           

            URI newUri = URI.create(
                originalUri.toString().replace(rawPath, encodedPath)
            );

            ServerHttpRequest newRequest = exchange.getRequest().mutate().uri(newUri).build();
            ServerWebExchange mutatedExchange = exchange.mutate().request(newRequest).build();

            return chain.filter(mutatedExchange);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 100;
    }
}