package ru.itmo.config;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

@CacheLogging
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class CacheLoggingInterceptor {

    private static final Logger LOGGER = Logger.getLogger(CacheLoggingInterceptor.class.getName());

    @Inject
    private CacheStatisticsService cacheStatisticsService;

    @AroundInvoke
    public Object logCacheStatistics(InvocationContext context) throws Exception {
        boolean loggingEnabled = false;
        try {
            loggingEnabled = cacheStatisticsService != null && cacheStatisticsService.isLoggingEnabled();
        } catch (Exception e) {
            return context.proceed();
        }

        if (!loggingEnabled) {
            return context.proceed();
        }

        long cachedObjectsBefore = 0;
        try {
            cachedObjectsBefore = cacheStatisticsService.getCacheHits();
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not get cache statistics before: " + e.getMessage());
        }

        Object result = context.proceed();

        try {
            long cachedObjectsAfter = cacheStatisticsService.getCacheHits();
            long newCachedObjects = cachedObjectsAfter - cachedObjectsBefore;

            String className = context.getTarget().getClass().getSimpleName();
            if (className.contains("$")) {
                className = className.substring(0, className.indexOf("$"));
            }

            CacheStatisticsService.CacheStats stats = cacheStatisticsService.getCurrentStats();

            LOGGER.info(String.format(
                    "[L2 Cache Stats] Method: %s.%s | Cached Objects: %d (+%d) | Details: %s",
                    className,
                    context.getMethod().getName(),
                    cachedObjectsAfter, newCachedObjects,
                    stats.getMessage()
            ));
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not get cache statistics after: " + e.getMessage());
        }

        return result;
    }
}

