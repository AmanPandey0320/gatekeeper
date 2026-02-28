package com.kabutar.gatekeeper.config;

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

    public static class Config{
        private TokenBucket tokenBucket;
        private LeakyBucket leakyBucket;

        public TokenBucket getTokenBucket() {
            return tokenBucket;
        }

        public void setTokenBucket(TokenBucket tokenBucket) {
            this.tokenBucket = tokenBucket;
        }

        public LeakyBucket getLeakyBucket() {
            return leakyBucket;
        }

        public void setLeakyBucket(LeakyBucket leakyBucket) {
            this.leakyBucket = leakyBucket;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "tokenBucket=" + tokenBucket +
                    ", leakyBucket=" + leakyBucket +
                    '}';
        }
    }

    public static class TokenBucket{
        private long capacity;
        private long refillRate;
        private String refillUnit;

        public long getCapacity() {
            return capacity;
        }

        public void setCapacity(long capacity) {
            this.capacity = capacity;
        }

        public long getRefillRate() {
            return refillRate;
        }

        public void setRefillRate(long refillRate) {
            this.refillRate = refillRate;
        }

        public String getRefillUnit() {
            return refillUnit;
        }

        public void setRefillUnit(String refillUnit) {
            this.refillUnit = refillUnit;
        }

        @Override
        public String toString() {
            return "TokenBucket{" +
                    "capacity=" + capacity +
                    ", refillRate=" + refillRate +
                    ", refillUnit='" + refillUnit + '\'' +
                    '}';
        }
    }

    public static class LeakyBucket{
        private int capacity;
        private int outFlowRate;
        private String outFlowUnit;

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public int getOutFlowRate() {
            return outFlowRate;
        }

        public void setOutFlowRate(int outFlowRate) {
            this.outFlowRate = outFlowRate;
        }

        public String getOutFlowUnit() {
            return outFlowUnit;
        }

        public void setOutFlowUnit(String outFlowUnit) {
            this.outFlowUnit = outFlowUnit;
        }

        @Override
        public String toString() {
            return "LeakyBucket{" +
                    "capacity=" + capacity +
                    ", outFlowRate=" + outFlowRate +
                    ", outFlowUnit='" + outFlowUnit + '\'' +
                    '}';
        }
    }

    public static class Rule{
        private String id;
        private String resourcePath;
        private List<String> limitBy;
        private String algorithm;
        private Config config;

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

        public List<String> getLimitBy() {
            return limitBy;
        }

        public void setLimitBy(List<String> limitBy) {
            this.limitBy = limitBy;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public Config getConfig() {
            return config;
        }

        public void setConfig(Config config) {
            this.config = config;
        }

        @Override
        public String toString() {
            return "Rule{" +
                    "id='" + id + '\'' +
                    ", resourcePath='" + resourcePath + '\'' +
                    ", limitBy=" + limitBy +
                    ", algorithm='" + algorithm + '\'' +
                    ", config=" + config +
                    '}';
        }
    }
}
