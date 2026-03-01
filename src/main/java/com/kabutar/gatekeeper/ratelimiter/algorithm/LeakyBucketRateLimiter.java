package com.kabutar.gatekeeper.ratelimiter.algorithm;

import com.kabutar.gatekeeper.config.RateLimitedConfig;
import com.kabutar.gatekeeper.ratelimiter.IdentityResolver;
import com.kabutar.gatekeeper.ratelimiter.RateLimiterConstants;
import com.kabutar.gatekeeper.ratelimiter.RateLimiterException;
import com.kabutar.gatekeeper.ratelimiter.defaults.DefaultTokenRule;
import com.kabutar.gatekeeper.ratelimiter.handler.RateLimitedHandler;
import com.kabutar.gatekeeper.util.PendingRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Leaky Bucket Rate Limiter implementation for Spring Cloud Gateway.
 *
 * Concept:
 *   Incoming requests are placed into a fixed-capacity queue (the "bucket").
 *   A scheduler drains the queue at a fixed rate (outFlowPerSec), forwarding
 *   one request per tick to the downstream chain. If the bucket is full when
 *   a new request arrives, it is immediately rejected with a 429 response.
 *
 * This enforces a smooth, constant output rate regardless of how bursty
 * the incoming traffic is.
 *
 * Key design decisions:
 *   - Mono.create() is used to keep the HTTP connection open (pending) until
 *     the scheduler drains the request. The sink is only resolved when the
 *     leaker fires or when the bucket is full.
 *   - Mono<Boolean> sink carries a signal: true = forward, false = already rejected.
 *     flatMap() then conditionally calls chain.filter() only for accepted requests,
 *     preventing double-execution on rejected paths.
 *   - ServerWebExchange is mutated with a deep-copied HttpHeaders before queuing
 *     to avoid ReadOnlyHttpHeaders.addAll() crash in NettyRoutingFilter when the
 *     exchange is forwarded across async thread boundaries.
 */
public class LeakyBucketRateLimiter implements RateLimiter {
    private static final Logger logger = LogManager.getLogger(LeakyBucketRateLimiter.class);

    private final RateLimitedHandler handler;
    private final RateLimitedConfig.LeakyBucket config;
    private final List<String> limitByDimensions;

    /*
     * Each unique identity (composite key built from limitBy dimensions) gets
     * its own Bucket with an independent queue and scheduler. This allows
     * per-user, per-IP, or per-API-key rate limiting independently.
     */
    private final Map<String, Bucket> buckets;

    public LeakyBucketRateLimiter(RateLimitedHandler handler, RateLimitedConfig.Rule rule) {
        this.handler = handler;
        if (rule == null) rule = DefaultTokenRule.getRule();
        this.config = rule.getConfig().getLeakyBucket();
        this.limitByDimensions = rule.getLimitBy();
        this.buckets = new HashMap<>();
        validateConfig();
        // Initialize the default bucket that applies globally to all requests
        // regardless of any per-dimension limiting.
        init(RateLimiterConstants.DEFAULT_LIMIT_DIMENSION);
    }

    /**
     * Creates a Bucket for the given identity and starts its leaker scheduler.
     *
     * The scheduler fires every (1000 / outFlowPerSec) milliseconds — this is
     * the inter-request interval derived from the desired output rate.
     * Each tick polls one request from the queue and signals its sink with
     * success(true), which unblocks the waiting Mono and triggers chain.filter().
     *
     * If the queue is empty on a tick, nothing happens — the tick is skipped silently.
     */
    private void init(String identity) {
        Bucket bucket = this.buckets.computeIfAbsent(identity, k -> new Bucket(this.config.getCapacity()));

        // Convert rate (req/sec) to period (ms/req): e.g. 10 req/s -> 100ms between each
        long intervalMs = 1000L / this.config.getOutFlowPerSec();

        bucket.getScheduler().scheduleAtFixedRate(() -> {
            PendingRequest pending = bucket.getQueue().poll(); // non-blocking, returns null if empty
            if (pending == null) return;

            logger.debug("Leaker releasing: {}",
                    pending.getServerWebExchange().getRequest().getURI().getPath());

            // Signal the waiting Mono.create sink that this request is approved.
            // true = accepted -> flatMap will call chain.filter() downstream.
            pending.getSink().success(true);
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    private void validateConfig() {
        if (config.getOutFlowPerSec() <= 0 || config.getCapacity() <= 0) {
            throw new RateLimiterException("Invalid config: " + config.toString());
        }
    }

    /**
     * Core allocation method called by the Gateway filter on every request.
     *
     * Flow:
     *   1. Deep-copy request headers into a mutable HttpHeaders instance.
     *      Netty seals inbound headers as ReadOnlyHttpHeaders. When this exchange
     *      is forwarded later across async boundaries, NettyRoutingFilter calls
     *      addAll() on the headers — this throws UnsupportedOperationException
     *      on ReadOnlyHttpHeaders. Explicitly copying headers into a fresh
     *      HttpHeaders object (backed by LinkedMultiValueMap) avoids this.
     *
     *   2. Wrap the exchange via mutate() with the mutable request headers.
     *      This mutableExchange is what gets queued and eventually forwarded.
     *
     *   3. Mono.create() returns a Mono<Boolean> that stays pending (HTTP
     *      connection held open) until either:
     *        a. The leaker drains the request -> sink.success(true)
     *        b. The bucket is full -> handler writes 429, then sink.success(false)
     *
     *   4. flatMap() acts as the conditional gate:
     *        - true  -> call chain.filter(mutableExchange) to forward the request
     *        - false -> return Mono.empty() since 429 is already written, nothing more to do
     *
     *   This separation is critical: without it, .then(chain.filter()) would execute
     *   for BOTH accepted and rejected requests, causing "response already committed" errors.
     */
    @Override
    public Mono<Void> allocate(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.debug("Entering allocate for: {}", exchange.getRequest().getURI().getPath());

        // Step 1: Deep-copy headers to make them mutable for downstream Gateway filters
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

        // Step 2: Resolve the bucket for this request's identity
        String compositeKey = buildCompositeKey(mutableExchange);
        if (!this.buckets.containsKey(compositeKey)) {
            init(compositeKey);
        }
        Bucket bucket = this.buckets.get(compositeKey);

        // Step 3 & 4: Queue the request and conditionally forward when drained
        return Mono.<Boolean>create(sink -> {
            PendingRequest request = new PendingRequest(mutableExchange, sink);
            boolean accepted = bucket.getQueue().offer(request); // non-blocking, returns false if full

            if (!accepted) {
                // Bucket is full — immediately reject with configured handler (typically 429)
                // After handler completes, signal false so flatMap skips chain.filter()
                logger.debug("BUCKET FULL - dropping: {}", exchange.getRequest().getURI().getPath());
                handler.handle(exchange)
                        .subscribe(null, sink::error, () -> sink.success(false));
            }
            // If accepted, sink stays open here.
            // The scheduler's leaker will call sink.success(true) when it drains this request.
        }).flatMap(accepted -> {
            if (accepted) {
                // Request was drained by the leaker — forward to the next filter/downstream service
                return chain.filter(mutableExchange);
            }
            // Request was rejected — handler already wrote the 429 response, nothing more to do
            return Mono.empty();
        });
    }

    /**
     * Builds a composite identity key from the configured limitBy dimensions.
     *
     * Examples:
     *   limitBy = []          -> "DEFAULT"
     *   limitBy = ["IP"]      -> "192.168.1.1"
     *   limitBy = ["IP","API"]-> "192.168.1.1:/posts/1"
     *
     * Each unique key gets its own independent Bucket with its own queue and scheduler,
     * enabling fine-grained rate limiting per user, IP, API key, etc.
     */
    private String buildCompositeKey(ServerWebExchange exchange) {
        if (limitByDimensions == null || limitByDimensions.isEmpty()) {
            return RateLimiterConstants.DEFAULT_LIMIT_DIMENSION;
        }
        return limitByDimensions.stream()
                .map(dim -> IdentityResolver.resolve(exchange, dim))
                .collect(Collectors.joining(":"));
    }

    /**
     * Internal representation of a leaky bucket.
     *
     * queue     : bounded blocking queue — the "bucket". Capacity = max requests
     *             that can wait before being dropped.
     * scheduler : single-threaded executor that fires at fixed intervals to drain
     *             one request per tick (the "leak").
     */
    private static class Bucket {
        private final ArrayBlockingQueue<PendingRequest> queue;
        private final ScheduledExecutorService scheduler;

        public Bucket(int size) {
            this.queue = new ArrayBlockingQueue<>(size);
            this.scheduler = Executors.newSingleThreadScheduledExecutor();
        }

        public ArrayBlockingQueue<PendingRequest> getQueue() { return queue; }
        public ScheduledExecutorService getScheduler() { return scheduler; }
    }
}
