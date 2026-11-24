package com.learning.ratelimiter.core.fixedwindow;

import com.learning.ratelimiter.core.RateLimitResult;
import com.learning.ratelimiter.core.RateLimiter;
import com.learning.ratelimiter.core.TimeSource;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class FixedWindowRateLimiter implements RateLimiter {
    private final int limit;
    private final long windowNanos;
    private final TimeSource timeSource;

    private final ConcurrentHashMap<String, Window> windows =
            new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(
            int limit,
            long windowDuration,
            TimeUnit timeUnit,
            TimeSource timeSource) {

        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }

        Objects.requireNonNull(timeUnit, "timeUnit cannot be null");
        this.timeSource = Objects.requireNonNull(
                timeSource, "timeSource cannot be null");

        this.windowNanos = timeUnit.toNanos(windowDuration);

        if (windowDuration <= 0 || windowNanos <= 0) {
            throw new IllegalArgumentException(
                    "Window duration must be positive and representable");
        }

        this.limit = limit;
    }

    @Override
    public RateLimitResult allowRequests(String userId) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        long initialWindow = Math.floorDiv(timeSource.nanoTime(), windowNanos);


        Window window = windows.computeIfAbsent(userId, k -> new Window(initialWindow, 0));
        synchronized (window) {
            long now = timeSource.nanoTime();
            long currentWindow = Math.floorDiv(now, windowNanos);
            if (window.windowId != currentWindow) {
                window.windowId = currentWindow;
                window.count = 0;
            }
            if (window.count >= limit) {
                long elapsed = Math.floorMod(now, windowNanos);
                long retryNanos = windowNanos - elapsed;
                long retryMillis = TimeUnit.NANOSECONDS.toMillis(retryNanos);
                if (retryMillis % 1000_00 != 0) {
                    retryMillis += 1;
                }
                return new RateLimitResult(false, 0, retryMillis);

            }
            window.count += 1;
            return new RateLimitResult(
                    true,
                    limit - window.count,
                    0
            );
        }
    }

    private static class Window {

        private long windowId;
        private int count;

        private Window(long windowId, int count) {
            this.windowId = windowId;
            this.count = count;
        }
    }
}
