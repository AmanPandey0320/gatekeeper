package com.kabutar.gatekeeper.ratelimiter.algorithm;

import com.kabutar.gatekeeper.config.rateLimit.FixedWindowConfig;
import com.kabutar.gatekeeper.config.rateLimit.Rule;
import com.kabutar.gatekeeper.ratelimiter.IdentityResolver;
import com.kabutar.gatekeeper.ratelimiter.RateLimiterConstants;
import com.kabutar.gatekeeper.ratelimiter.RateLimiterException;
import com.kabutar.gatekeeper.ratelimiter.handler.RateLimitedHandler;
import com.kabutar.gatekeeper.util.Units;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedWindowCounterRatelimiter implements RateLimiter{
    private Logger logger = LogManager.getLogger(FixedWindowCounterRatelimiter.class);
    private RateLimitedHandler handler;
    private FixedWindowConfig config;
    private List<String> limitBy;

    private Map<String, Window> windowMap;

    public FixedWindowCounterRatelimiter(RateLimitedHandler handler, Rule rule){
        this.handler = handler;
        this.config = rule.getConfig().getFixedWindow();
        this.windowMap = new HashMap<>();
        this.limitBy = rule.getLimitBy();

        validateConfig();
        init(RateLimiterConstants.DEFAULT_LIMIT_IDENTITY);
    }

    /**
     * validates config
     */
    private void validateConfig(){
        if(this.config.getCounter() > 0 && this.config.getTimeWindow() > 0 && Units.Time.MULTIPLIER.containsKey(this.config.getTimeUnit())){
            return;
        }
        throw new RateLimiterException("Invalid Config " + this.config.toString()+ " for : " + FixedWindowCounterRatelimiter.class.getName());
    }

    private void init(String identity){
        Window window = this.windowMap.computeIfAbsent(identity,
                k -> new Window(
                        this.config.getCounter(),
                        this.config.getTimeWindow() * Units.Time.MULTIPLIER.get(this.config.getTimeUnit())));


    }

    @Override
    public Mono<Void> allocate(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.debug("Entering allocate method of {} for path: {}",
                FixedWindowCounterRatelimiter.class.getCanonicalName(),
                exchange.getRequest().getPath());
        HttpHeaders mutableHeaders = new HttpHeaders();
        mutableHeaders.putAll(exchange.getRequest().getHeaders());

        ServerHttpRequest mutableRequest = exchange.getRequest()
                .mutate()
                .headers(h -> {
                    h.clear();
                    h.putAll(mutableHeaders);
                })
                .build();

        ServerWebExchange mutableExchange = exchange.mutate()
                .request(mutableRequest)
                .build();
        return Mono.create((MonoSink<Boolean> sink) -> {
            boolean isAccepted = true;

            int successTill = -1;
            for(String dimension: this.limitBy){
                String identity = IdentityResolver.resolve(exchange,dimension);
                if(!this.windowMap.containsKey(identity)){
                    init(identity);
                }
                Window window = this.windowMap.get(identity);
                isAccepted = isAccepted && window.tryIncrement();

                if(isAccepted){
                    successTill++;
                }else{
                    break;
                }
            }
            if(!isAccepted){
                logger.debug("COUNTER FULL - dropping: {}", exchange.getRequest().getURI().getPath());
                for(int i=0;i<=successTill;i++){
                    String identity = IdentityResolver.resolve(exchange,this.limitBy.get(i));
                    this.windowMap.get(identity).restore();
                }
                handler.handle(exchange)
                        .subscribe(null, sink::error, () -> sink.success(false));
            }else{
                sink.success(true);
            }
        }).flatMap(isAccepted -> {
            if(isAccepted){
                logger.debug("Exiting allocate for: {}", exchange.getRequest().getURI().getPath());
                return chain.filter(mutableExchange);
            }
            return Mono.empty();
        });
    }

    private static class Window{
        private int counter;
        private  int period;
        private final AtomicInteger currentCount;
        private final ScheduledExecutorService scheduler;

        public Window (int counter, int period){
            this.counter = counter;
            this.period = period;
            this.currentCount = new AtomicInteger(0);
            this.scheduler = Executors.newSingleThreadScheduledExecutor();
            init();
        }

        private void init() {
            scheduler.scheduleAtFixedRate(
                    () -> currentCount.set(0),
                    0,   // initial delay — reset starts after first window
                    period,   // repeat every `period` seconds
                    TimeUnit.SECONDS
            );
        }

        // Returns true if request is allowed, false if limit exceeded
        public boolean tryIncrement() {
            return currentCount.incrementAndGet() <= counter;
        }

        public void restore() {
            currentCount.decrementAndGet();
        }
    }
}
