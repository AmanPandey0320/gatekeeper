package com.kabutar.gatekeeper.ratelimiter.defaults;

import com.kabutar.gatekeeper.config.RateLimitedConfig;
import com.kabutar.gatekeeper.ratelimiter.RateLimiterConstants;

import java.time.Instant;
import java.util.List;

public class DefaultTokenRule {
    private static volatile RateLimitedConfig.Rule rule = null;
    private DefaultTokenRule() {}

    public static RateLimitedConfig.Rule getRule()
    {
        if (rule == null) {
            // To make thread safe
            synchronized (DefaultTokenRule.class)
            {
                // check again as multiple threads can reach above step
                if (rule == null){
                    rule = new RateLimitedConfig.Rule();
                    RateLimitedConfig.Config conf = new RateLimitedConfig.Config();
                    RateLimitedConfig.TokenBucket tokenBucket = new RateLimitedConfig.TokenBucket();

                    tokenBucket.setCapacity(RateLimiterConstants.TokenBucket.DEFAULT_CAPACITY);
                    tokenBucket.setRefillRate(RateLimiterConstants.TokenBucket.DEFAULT_REFILL_RATE);
                    tokenBucket.setRefillUnit(RateLimiterConstants.TokenBucket.DEFAULT_UNIT);

                    conf.setTokenBucket(tokenBucket);

                    rule.setLimitBy(List.of());
                    rule.setAlgorithm(RateLimiterConstants.Algorithm.TOKEN_BUCKET);
                    rule.setConfig(conf);
                    rule.setId(String.valueOf(Instant.now().toEpochMilli()));
                    rule.setResourcePath("/**");

                }
            }
        }
        return rule;
    }
}
