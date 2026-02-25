package com.kabutar.gatekeeper.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.kabutar.gatekeeper.config.ProtectedRouteConfig;

import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(prefix = "config.protected-routes", name = "enabled", havingValue = "true")
public class ProtectedRouteFilter implements GlobalFilter,Ordered {
	
	private ProtectedRouteConfig routeConfig;
	
	@Autowired
	public void setRouteConfig(ProtectedRouteConfig routeConfig) {
		this.routeConfig = routeConfig;
	}

	@Override
	public int getOrder() {
		return FilterOrder.PROTECTED_ROUTE_FILTER_ORDER;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// make http request to check resource
		return chain.filter(exchange).then(Mono.fromRunnable(() -> {
//			System.out.println("Gateway filter executed");
		}));
	}
	
	
	
}
