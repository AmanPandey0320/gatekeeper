package com.kabutar.gatekeeper.util;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.MonoSink;

public class PendingRequest {
    private ServerWebExchange serverWebExchange;
    private MonoSink<Boolean> sink;

    public ServerWebExchange getServerWebExchange() {
        return serverWebExchange;
    }

    public MonoSink<Boolean> getSink() {
        return sink;
    }

    public PendingRequest(ServerWebExchange serverWebExchange, MonoSink<Boolean> sink) {
        this.serverWebExchange = serverWebExchange;
        this.sink = sink;
    }
}
