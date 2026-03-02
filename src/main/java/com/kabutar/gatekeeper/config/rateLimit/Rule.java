package com.kabutar.gatekeeper.config.rateLimit;

import java.util.List;

public class Rule{
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

    public static class Config{
        private TokenBucketConfig tokenBucket;
        private LeakyBucketConfig leakyBucket;

        public TokenBucketConfig getTokenBucket() {
            return tokenBucket;
        }

        public void setTokenBucket(TokenBucketConfig tokenBucket) {
            this.tokenBucket = tokenBucket;
        }

        public LeakyBucketConfig getLeakyBucket() {
            return leakyBucket;
        }

        public void setLeakyBucket(LeakyBucketConfig leakyBucket) {
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
}
