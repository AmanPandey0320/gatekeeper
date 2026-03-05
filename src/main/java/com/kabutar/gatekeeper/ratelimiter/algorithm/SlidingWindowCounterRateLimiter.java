package com.kabutar.gatekeeper.ratelimiter.algorithm;

import com.kabutar.gatekeeper.config.rateLimit.Rule;
import com.kabutar.gatekeeper.config.rateLimit.SlidingWindowConfig;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class SlidingWindowCounterRateLimiter implements RateLimiter {

    private final Logger logger = LogManager.getLogger(SlidingWindowCounterRateLimiter.class);
    private final RateLimitedHandler handler;
    private final SlidingWindowConfig config;
    private final List<String> limitBy;
    private final Map<String, Window> windowMap;

    public SlidingWindowCounterRateLimiter(RateLimitedHandler handler, Rule rule) {
        this.handler = handler;
        this.config = rule.getConfig().getSlidingWindow();
        this.windowMap = new HashMap<>();
        this.limitBy = rule.getLimitBy();

        validateConfig();
        init(RateLimiterConstants.DEFAULT_LIMIT_IDENTITY);
    }

    private void validateConfig() {
        if (this.config.getCounter() > 0
                && this.config.getTimeWindow() > 0
                && Units.Time.MULTIPLIER.containsKey(this.config.getTimeUnit())) {
            return;
        }
        throw new RateLimiterException(
                "Invalid Config " + this.config.toString() + " for : "
                        + SlidingWindowCounterRateLimiter.class.getName());
    }

    private void init(String identity) {
        this.windowMap.computeIfAbsent(identity,
                k -> new Window(
                        this.config.getCounter(),
                        this.config.getTimeWindow() * Units.Time.MULTIPLIER.get(this.config.getTimeUnit())));
    }

    @Override
    public Mono<Void> allocate(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.debug("Entering allocate method of {} for path: {}",
                SlidingWindowCounterRateLimiter.class.getCanonicalName(),
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

            for (String dimension : this.limitBy) {
                String identity = IdentityResolver.resolve(exchange, dimension);
                if (!this.windowMap.containsKey(identity)) {
                    init(identity);
                }
                Window window = this.windowMap.get(identity);
                isAccepted = isAccepted && window.tryIncrement();

                if (isAccepted) {
                    successTill++;
                } else {
                    break;
                }
            }

            if (!isAccepted) {
                logger.debug("SLIDING COUNTER FULL - dropping: {}", exchange.getRequest().getURI().getPath());
                // Rollback all successfully incremented dimensions
                for (int i = 0; i <= successTill; i++) {
                    String identity = IdentityResolver.resolve(exchange, this.limitBy.get(i));
                    this.windowMap.get(identity).restore();
                }
                handler.handle(exchange)
                        .subscribe(null, sink::error, () -> sink.success(false));
            } else {
                sink.success(true);
            }
        }).flatMap(isAccepted -> {
            if (isAccepted) {
                logger.debug("Exiting allocate for: {}", exchange.getRequest().getURI().getPath());
                return chain.filter(mutableExchange);
            }
            return Mono.empty();
        });
    }


    /**
     * -------------------------------------------------------------------------
     * Inner Window class — tracks previous + current fixed windows
     * -------------------------------------------------------------------------
    */
    private static class Window {
        private final int limit;
        private final long periodSeconds;

        // Current window
        private final AtomicInteger currentCount;
        private final AtomicLong currentWindowStart;

        // Previous window count (snapshot at rollover)
        private final AtomicInteger previousCount;

        private final ScheduledExecutorService scheduler;
        private final ReentrantLock rolloverLock = new ReentrantLock();

        public Window(int limit, int periodSeconds) {
            this.limit = limit;
            this.periodSeconds = periodSeconds;
            this.currentCount = new AtomicInteger(0);
            this.previousCount = new AtomicInteger(0);
            this.currentWindowStart = new AtomicLong(System.currentTimeMillis());
            this.scheduler = Executors.newSingleThreadScheduledExecutor();
            init();
        }

        private void init() {
            // Roll window forward every `periodSeconds`
            scheduler.scheduleAtFixedRate(
                    this::rollover,
                    periodSeconds,
                    periodSeconds,
                    TimeUnit.SECONDS
            );
        }

        private void rollover() {
            rolloverLock.lock();
            try {
                // Snapshot current → previous, reset current
                previousCount.set(currentCount.get());
                currentCount.set(0);
                currentWindowStart.set(System.currentTimeMillis());
            } finally {
                rolloverLock.unlock();
            }
        }

        /**
         * Returns true if request is allowed under the sliding window estimate.
         *
         * Formula:
         *   elapsedRatio  = elapsedMs / windowMs        (0.0 → 1.0)
         *   overlapRatio  = 1 - elapsedRatio            (previous window's weight)
         *   estimated     = previous * overlapRatio + current
         */
        public boolean tryIncrement() {
            rolloverLock.lock();
            try {
                long elapsedMs = System.currentTimeMillis() - currentWindowStart.get();
                long windowMs = periodSeconds * 1000L;

                double overlapRatio = 1.0 - ((double) elapsedMs / windowMs);
                // Clamp to [0.0, 1.0] — guard against clock skew or scheduler lag
                overlapRatio = Math.max(0.0, Math.min(1.0, overlapRatio));

                double estimated = (previousCount.get() * overlapRatio) + currentCount.get();

                if (estimated < limit) {
                    currentCount.incrementAndGet();
                    return true;
                }
                return false;
            } finally {
                rolloverLock.unlock();
            }
        }

        public void restore() {
            currentCount.decrementAndGet();
        }
    }
}
