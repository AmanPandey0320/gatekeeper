package com.kabutar.gatekeeper.ratelimiter;

import org.springframework.web.server.ServerWebExchange;

public class IdentityResolver {
    public static String resolve(ServerWebExchange exchange, String dimension){
        switch (dimension.toLowerCase()) {
            case "ip" -> {
                java.net.InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
                String ip = (remoteAddress != null)
                        ? remoteAddress.getAddress().getHostAddress()
                        : "unknown";
                return "ip="+ip;
            }
            case "userid" -> {
                String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
                return "userId=" + (userId != null ? userId : "anonymous");
            }
            case "apikey" -> {
                String apiKey = exchange.getRequest().getHeaders().getFirst("X-Api-Key");
                return "apiKey=" + (apiKey != null ? apiKey : "unknown");
            }
        }
        return RateLimiterConstants.DEFAULT_LIMIT_DIMENSION;
    }
}
