package com.learning.ratelimiter.core;

@FunctionalInterface
public interface TimeSource {

    long nanoTime();
    //long currentTimeMillis();
}