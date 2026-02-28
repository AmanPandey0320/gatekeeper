package com.kabutar.gatekeeper.ratelimiter.algorithm;

import com.kabutar.gatekeeper.config.RateLimitedConfig;
import com.kabutar.gatekeeper.ratelimiter.RateLimiterConstants;
import com.kabutar.gatekeeper.ratelimiter.RateLimiterException;
import com.kabutar.gatekeeper.ratelimiter.defaults.DefaultTokenRule;
import com.kabutar.gatekeeper.ratelimiter.handler.RateLimitedHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

public class LeakyBucketRateLimiter implements RateLimiter{
    private static final Logger logger  = LogManager.getLogger(LeakyBucketRateLimiter.class);

    private RateLimitedHandler handler;
    private RateLimitedConfig.LeakyBucket config;
    private List<String> limitByDimensions;

    private Map<String, Bucket> buckets;


    public LeakyBucketRateLimiter(RateLimitedHandler handler, RateLimitedConfig.Rule rule){
        this.handler = handler;

        if(rule == null){
            rule = DefaultTokenRule.getRule();
        }

        this.config = rule.getConfig().getLeakyBucket();
        this.limitByDimensions = rule.getLimitBy();
        this.buckets = new HashMap<>();

        validateConfig();

        //initialize default route bucket
        if(limitByDimensions.isEmpty()){
            this.buckets.put(RateLimiterConstants.DEFAULT_LIMIT_DIMENSION, new Bucket(this.config.getCapacity()));
        }
    }

    private void init(){

    }

    private void validateConfig(){
        if( config.getOutFlowRate() <= 0 || config.getCapacity() <= 0){
            throw new RateLimiterException("Invalid config: " +config.toString());
        }
    }
    @Override
    public boolean allocate(ServerWebExchange exchange) {
        // TODO: implement leaky bucket
        logger.debug("Entering allocate method of Leaky bucket algorithm for request: {}",exchange.getRequest().getURI().getPath());

        return true;
    }

    @Override
    public Mono<Void> handleRateLimited(ServerWebExchange exchange) {
        return handler.handle(exchange);
    }

    private static class Bucket{
        private ArrayBlockingQueue<ServerWebExchange> queue;
        private ScheduledExecutorService scheduler;

        public Bucket(int size) {
            this.queue = new ArrayBlockingQueue<>(size);
        }

        public ArrayBlockingQueue<ServerWebExchange> getQueue() {
            return queue;
        }

        public void setQueue(ArrayBlockingQueue<ServerWebExchange> queue) {
            this.queue = queue;
        }

        public ScheduledExecutorService getScheduler() {
            return scheduler;
        }

        public void setScheduler(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
        }
    }
}
