package com.learning.ratelimiter.core;

public record RateLimitResult(
        boolean allowed,
        long remainingRequests,
        long retryAfterMillis
) {
}