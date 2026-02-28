package com.kabutar.gatekeeper.ratelimiter.algorithm;

import com.kabutar.gatekeeper.config.RateLimitedConfig;
import com.kabutar.gatekeeper.ratelimiter.RateLimiterConstants;
import com.kabutar.gatekeeper.ratelimiter.RateLimiterException;
import com.kabutar.gatekeeper.ratelimiter.defaults.DefaultTokenRule;
import com.kabutar.gatekeeper.ratelimiter.handler.RateLimitedHandler;
import com.kabutar.gatekeeper.util.Units;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenBucketRateLimiter implements RateLimiter{
    private static final Logger logger = LogManager.getLogger(TokenBucketRateLimiter.class);

    private RateLimitedHandler handler;
    private RateLimitedConfig.TokenBucket config;
    private List<String> limitByDimensions;

    private Map<String,Bucket> buckets;


    public TokenBucketRateLimiter(RateLimitedHandler handler, RateLimitedConfig.Rule rule){
        this.handler = handler;

        if(rule == null){
            rule = DefaultTokenRule.getRule();
        }

        this.config = rule.getConfig().getTokenBucket();
        this.limitByDimensions = rule.getLimitBy();
        this.buckets = new HashMap<>();

        validateConfig();

        //initialize default route bucket
        if(limitByDimensions.isEmpty()){
            this.buckets.put(RateLimiterConstants.DEFAULT_LIMIT_DIMENSION, new Bucket(config.getCapacity()));
        }
    }

    private void validateConfig(){
        if( config.getRefillRate() <= 0 || config.getCapacity() <= 0){
            throw new RateLimiterException("Invalid config: " +config.toString());
        }
    }

    // fills bucket
    private void fillBucket(Bucket bucket){
        long currTime = Instant.now().toEpochMilli();
        long timeDiff = currTime - bucket.getLastFilled();
        long multiplier = timeDiff/(Units.Time.MULTIPLIER.get(config.getRefillUnit()) * 1000);

        if(multiplier > 0){
            bucket.setTokens( Math.min(config.getCapacity(),bucket.getTokens() + multiplier*config.getRefillRate()));
            bucket.setLastFilled(currTime);
        }
    }

    private boolean isAllocateable(String identity, ServerWebExchange exchange){
        Bucket bucket = this.buckets.computeIfAbsent(identity, k -> new Bucket(config.getCapacity()));
        synchronized (bucket){
            fillBucket(bucket);
        }
        return bucket.getTokens() > 0;
    }

    private void removeTokenFromBucket(String identity){
        Bucket bucket = this.buckets.computeIfAbsent(identity, k -> new Bucket(config.getCapacity()));
        synchronized (bucket){
            bucket.setTokens(bucket.getTokens() - 1);
        }
    }


    @Override
    public boolean allocate(ServerWebExchange exchange) {
        logger.debug("Entering allocate method of Token bucket rate limiting algorithm");

        // if no limit parameter is defined
        // use default route parameter
        if(limitByDimensions.isEmpty()){
            if(isAllocateable(RateLimiterConstants.DEFAULT_LIMIT_DIMENSION,exchange)){
                removeTokenFromBucket(RateLimiterConstants.DEFAULT_LIMIT_DIMENSION);
                return true;
            }
            logger.debug("Ratelimited request at: {} by dimension: {}",exchange.getRequest().getURI(),RateLimiterConstants.DEFAULT_LIMIT_DIMENSION);
            return false;
        }
        boolean canAllocate =  true;
        for(String dimension:limitByDimensions){
            canAllocate = canAllocate && isAllocateable(dimension,exchange);
            if(!canAllocate){
                logger.debug("Ratelimited request at: {} by dimension: {}",exchange.getRequest().getURI(),dimension);
                return false;
            }
        }

        for(String dimension:this.limitByDimensions){
            removeTokenFromBucket(dimension);
        }
        logger.debug("Exiting allocate method of Token bucket rate limiting algorithm");
        return true;
    }

    @Override
    public Mono<Void> handleRateLimited(ServerWebExchange exchange) {
        return handler.handle(exchange);
    }

    private static class Bucket{
        private long tokens;
        private long lastFilled;

        public Bucket(long tokens) {
            this.tokens = tokens;
            this.lastFilled = Instant.now().toEpochMilli();
        }

        public long getTokens() {
            return tokens;
        }

        public void setTokens(long tokens) {
            this.tokens = tokens;
        }

        public long getLastFilled() {
            return lastFilled;
        }

        public void setLastFilled(long lastFilled) {
            this.lastFilled = lastFilled;
        }
    }

}
