package com.learning.ratelimiter.core;

public interface RateLimiter {

    RateLimitResult allowRequests(String key);
}