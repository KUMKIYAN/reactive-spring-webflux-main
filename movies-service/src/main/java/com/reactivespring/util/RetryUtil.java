package com.reactivespring.util;

import com.reactivespring.exception.MoviesInfoServerException;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;

import java.time.Duration;

public class RetryUtil {


    public static Retry retrySpec() {
       return Retry.backoff(3, Duration.ofMillis(500))
                // only for server exceptions not for client exception retry
                .filter(ex -> ex instanceof MoviesInfoServerException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    return Exceptions.propagate(retrySignal.failure());
                });
    }
}
