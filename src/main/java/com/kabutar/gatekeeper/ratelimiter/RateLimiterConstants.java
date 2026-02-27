package com.kabutar.gatekeeper.ratelimiter;

public class RateLimiterConstants {
    public static final class Algorithm{
        public static final String TOKEN_BUCKET = "tokenBucket";
    }
    public static String DEFAULT_LIMIT_DIMENSION = "default";

    public static class  TokenBucket {
        public static long DEFAULT_CAPACITY = 1000;
        public static long DEFAULT_REFILL_RATE = 100;
        public static String DEFAULT_UNIT = "S";
    }
}
