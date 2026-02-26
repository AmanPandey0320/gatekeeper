package com.kabutar.gatekeeper.ratelimiter.factory;

import com.kabutar.gatekeeper.config.RateLimitedConfig;
import com.kabutar.gatekeeper.ratelimiter.RateLimiterException;
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

@Component
public class RateLimiterFactoryImpl implements RateLimiterFactory {
    private final static Logger logger = LogManager.getLogger(RateLimiterFactoryImpl.class);
    private final static PathPatternParser parser = new PathPatternParser();

    private RateLimitedConfig rateLimitedConfig;
    private RateLimitedHandler handler;
    private RateLimiter defaultRateLimiter;

    private List<PathPattern> pathPatterns;
    private List<RateLimiter> rateLimiters;

    @Autowired
    public RateLimiterFactoryImpl(RateLimitedConfig rateLimitedConfig, RateLimitedHandler handler){
        this.rateLimitedConfig = rateLimitedConfig;
        this.handler = handler;

        this.rateLimiters = new ArrayList<>();
        this.pathPatterns = new ArrayList<>();

        initializeDefaultRateLimiter(rateLimitedConfig);
        processRules(rateLimitedConfig);
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

    // factory methods

    //factory method for default rate limited
    @Override
    public RateLimiter get() {
        return defaultRateLimiter;
    }

    //factory method for route based filter
    @Override
    public RateLimiter get(ServerWebExchange exchange) {
        PathContainer pathContainer = exchange.getRequest().getPath().pathWithinApplication();
        for(int i=0;i<pathPatterns.size();i++){
            if(pathPatterns.get(i).matches(pathContainer)){
                return rateLimiters.get(i);
            }
        }
        return defaultRateLimiter;
    }

    @Override
    public RateLimiter init(String algorithm) {
        return init(algorithm,null);
    }

    @Override
    public RateLimiter init(String algorithm, Object object) {
        if(algorithm.equals(RateLimiterConstants.Algorithm.TOKEN_BUCKET)){
            return new TokenBucketRateLimiter(handler, (RateLimitedConfig.Rule) object);
        }
        throw new RateLimiterException("Invalid rate limiter algorithm: "+ algorithm);
    }
}
