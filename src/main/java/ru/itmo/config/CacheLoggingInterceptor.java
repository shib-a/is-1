package ru.itmo.config;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.sessions.Session;

import java.util.logging.Logger;

@CacheLogging
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class CacheLoggingInterceptor {

    private static final Logger LOGGER = Logger.getLogger(CacheLoggingInterceptor.class.getName());

    @Inject
    private EntityManager entityManager;

    @Inject
    private CacheStatisticsService cacheStatisticsService;

    @AroundInvoke
    public Object logCacheStatistics(InvocationContext context) throws Exception {
        boolean loggingEnabled = cacheStatisticsService.isLoggingEnabled();

        long cacheHitsBefore = 0;
        long cacheMissesBefore = 0;

        if (loggingEnabled) {
            try {
                Session session = getSession();
                if (session != null) {
                    cacheHitsBefore = getCacheHits(session);
                    cacheMissesBefore = getCacheMisses(session);
                }
            } catch (Exception e) {
                LOGGER.warning("Could not get cache statistics before: " + e.getMessage());
            }
        }
        Object result = context.proceed();

        if (loggingEnabled) {
            try {
                Session session = getSession();
                if (session != null) {
                    long cacheHitsAfter = getCacheHits(session);
                    long cacheMissesAfter = getCacheMisses(session);

                    long newHits = cacheHitsAfter - cacheHitsBefore;
                    long newMisses = cacheMissesAfter - cacheMissesBefore;

                    LOGGER.info(String.format(
                            "[L2 Cache Stats] Method: %s.%s | Cache Hits: %d (+%d) | Cache Misses: %d (+%d)",
                            context.getTarget().getClass().getSimpleName(),
                            context.getMethod().getName(),
                            cacheHitsAfter, newHits,
                            cacheMissesAfter, newMisses
                    ));
                }
            } catch (Exception e) {
                LOGGER.warning("Could not get cache statistics after: " + e.getMessage());
            }
        }

        return result;
    }

    private Session getSession() {
        try {
            if (entityManager.getDelegate() instanceof EntityManagerImpl) {
                return ((EntityManagerImpl) entityManager.getDelegate()).getServerSession();
            }
        } catch (Exception e) {
            LOGGER.warning("Could not get session: " + e.getMessage());
        }
        return null;
    }

    private long getCacheHits(Session session) {
        return cacheStatisticsService.getCacheHits();
    }

    private long getCacheMisses(Session session) {
        return cacheStatisticsService.getCacheMisses();
    }
}

