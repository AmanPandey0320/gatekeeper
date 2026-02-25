package com.kabutar.gatekeeper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "config.rateLimited")
public class RateLimitedConfig {
}
