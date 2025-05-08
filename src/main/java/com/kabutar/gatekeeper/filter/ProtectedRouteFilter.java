package com.kabutar.gatekeeper.filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(prefix = "config.protected-routes", name = "enabled", havingValue = "true")
public class ProtectedRouteFilter {
	
}
