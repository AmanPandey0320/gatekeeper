package com.kabutar.gatekeeper.filter;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UriReplaceFilterFactory implements GlobalFilter, Ordered {
    private static Logger logger = LogManager.getLogger(UriReplaceFilterFactory.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.debug("Entering UriReplaceFilter...");
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

            logger.debug("Exiting UriReplaceFilter...");
            return chain.filter(mutatedExchange);
        }
        logger.debug("Exiting UriReplaceFilter...");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return FilterOrder.URI_REPLACE_FILTER_ORDER;
    }
}