package com.kabutar.gatekeeper.config.rateLimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "config.rate-limited")
public class RateLimitedConfig {
    private boolean enabled;
    private String algorithm;
    private String strategy;
    private List<Rule> rules;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    @Override
    public String toString() {
        return "RateLimitedConfig{" +
                "enabled=" + enabled +
                ", algorithm='" + algorithm + '\'' +
                ", strategy='" + strategy + '\'' +
                ", rules=" + rules +
                '}';
    }

}
