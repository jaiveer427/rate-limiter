package com.learning;

import com.learning.ratelimiter.core.fixedwindow.FixedWindowRateLimiter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 *
 */
import java.util.concurrent.*;

public class RateLimiterCoreApp {

    public static void main(String[] args) throws Exception {

        FixedWindowRateLimiter limiter =
                new FixedWindowRateLimiter(
                        5,
                        10,
                        TimeUnit.SECONDS,
                        System::nanoTime
                );

        int totalRequests = 100;
        int threadCount = 10;

        CountDownLatch startLatch = new CountDownLatch(1);

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        try {
            var futures = new java.util.ArrayList<Future<Boolean>>();

            for (int i = 0; i < totalRequests; i++) {

                futures.add(executor.submit(() -> {
                    startLatch.await();

                    return limiter
                            .allowRequests("user-101")
                            .allowed();
                }));
            }

            startLatch.countDown();

            int allowed = 0;
            int rejected = 0;

            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    allowed++;
                } else {
                    rejected++;
                }
            }

            System.out.println("Allowed: " + allowed);
            System.out.println("Rejected: " + rejected);


        } finally {
            executor.shutdown();
        }
    }
}

