package com.kabutar.gatekeeper.ratelimiter.factory;

import com.kabutar.gatekeeper.config.RateLimitedConfig;
import com.kabutar.gatekeeper.ratelimiter.RateLimiterException;
import com.kabutar.gatekeeper.ratelimiter.algorithm.LeakyBucketRateLimiter;
import com.kabutar.gatekeeper.ratelimiter.algorithm.RateLimiter;
import com.kabutar.gatekeeper.ratelimiter.RateLimiterConstants;
import com.kabutar.gatekeeper.ratelimiter.algorithm.TokenBucketRateLimiter;
import com.kabutar.gatekeeper.ratelimiter.handler.RateLimitedHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class RateLimiterFactoryImpl implements RateLimiterFactory {
    private final static Logger logger = LogManager.getLogger(RateLimiterFactoryImpl.class);
    private final static PathPatternParser parser = new PathPatternParser();

    private RateLimitedHandler handler;
    private RateLimiter defaultRateLimiter;

    private List<PathPattern> pathPatterns;
    private List<RateLimiter> rateLimiters;

    private final Map<String, Function<RateLimitedConfig.Rule, RateLimiter>> ALGORITHM_REGISTRY;

    @Autowired
    public RateLimiterFactoryImpl(RateLimitedConfig rateLimitedConfig, RateLimitedHandler handler){
        this.handler = handler;

        this.rateLimiters = new ArrayList<>();
        this.pathPatterns = new ArrayList<>();

        ALGORITHM_REGISTRY = this.initializeAlgorithmMap();
        initializeDefaultRateLimiter(rateLimitedConfig);
        processRules(rateLimitedConfig);
    }

    // add new algorithms here without touching any existing logic
    private Map<String, Function<RateLimitedConfig.Rule, RateLimiter>> initializeAlgorithmMap(){
        return Map.of(
                RateLimiterConstants.Algorithm.TOKEN_BUCKET, rule -> new TokenBucketRateLimiter(handler, rule),
                RateLimiterConstants.Algorithm.LEAKY_BUCKET, rule -> new LeakyBucketRateLimiter(handler,rule)
                //add new rate limited algorithms here
        );
    }

    /**
     *
     * @param rateLimitedConfig
     */
    private void initializeDefaultRateLimiter(RateLimitedConfig rateLimitedConfig){
        this.defaultRateLimiter = this.init(rateLimitedConfig.getAlgorithm());
    }

    /**
     *
     * @param config
     */
    private void processRules(RateLimitedConfig config){
        for(RateLimitedConfig.Rule rule: config.getRules()){
            pathPatterns.add(parser.parse(rule.getResourcePath()));
            rateLimiters.add(this.init(rule.getAlgorithm(),rule));
        }
    }

    //factory method for default rate limited
    @Override
    public RateLimiter get() {
        logger.debug("Using default rate limiter");
        return defaultRateLimiter;
    }

    //factory method for route based filter
    @Override
    public RateLimiter get(ServerWebExchange exchange) {
        PathContainer pathContainer = exchange.getRequest().getPath().pathWithinApplication();
        for(int i=0;i<pathPatterns.size();i++){
            if(pathPatterns.get(i).matches(pathContainer)){
                logger.debug("Found route specific rate limiter for path {}",pathContainer.value());
                return rateLimiters.get(i);
            }
        }
        logger.debug("No route specific rate limiter found for path {}",pathContainer.value());
        return get();
    }

    @Override
    public RateLimiter init(String algorithm) {
        return init(algorithm,null);
    }

    @Override
    public RateLimiter init(String algorithm, Object config) {
        RateLimitedConfig.Rule rule = (RateLimitedConfig.Rule) config;
        return Optional.ofNullable(ALGORITHM_REGISTRY.get(algorithm))
                .map(fn -> fn.apply(rule))
                .orElseThrow(() -> new RateLimiterException("Invalid algorithm: " + algorithm));
    }
}
