package com.kabutar.gatekeeper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "config.rate-limited")
public class RateLimitedConfig {
    private boolean enabled;
    private String algorithm;
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

    @Override
    public String toString() {
        return "RateLimitedConfig{" +
                "enabled=" + enabled +
                ", algorithm='" + algorithm + '\'' +
                ", rules=" + rules +
                '}';
    }

    public static class Rule{
        private String id;
        private String resourcePath;
        private String unit;
        private String limitPerUnit;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getResourcePath() {
            return resourcePath;
        }

        public void setResourcePath(String resourcePath) {
            this.resourcePath = resourcePath;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getLimitPerUnit() {
            return limitPerUnit;
        }

        public void setLimitPerUnit(String limitPerUnit) {
            this.limitPerUnit = limitPerUnit;
        }

        @Override
        public String toString() {
            return "Rule{" +
                    "id='" + id + '\'' +
                    ", resourcePath='" + resourcePath + '\'' +
                    ", unit='" + unit + '\'' +
                    ", limitPerUnit='" + limitPerUnit + '\'' +
                    '}';
        }
    }
}
