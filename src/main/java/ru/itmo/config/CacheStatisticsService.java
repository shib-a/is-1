package ru.itmo.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.server.ServerSession;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;


@ApplicationScoped
public class CacheStatisticsService {

    private static final Logger LOGGER = Logger.getLogger(CacheStatisticsService.class.getName());

    private final AtomicBoolean loggingEnabled = new AtomicBoolean(false);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    @Inject
    private EntityManager entityManager;

    @PostConstruct
    public void init() {
        String enabled = System.getProperty("cache.logging.enabled", "false");
        loggingEnabled.set(Boolean.parseBoolean(enabled));
        LOGGER.info("Cache logging initialized. Enabled: " + loggingEnabled.get());
    }

    public void enableLogging() {
        loggingEnabled.set(true);
        LOGGER.info("Cache statistics logging ENABLED");
    }

    public void disableLogging() {
        loggingEnabled.set(false);
        LOGGER.info("Cache statistics logging DISABLED");
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled.get();
    }

    public boolean toggleLogging() {
        boolean newState = !loggingEnabled.get();
        loggingEnabled.set(newState);
        LOGGER.info("Cache statistics logging " + (newState ? "ENABLED" : "DISABLED"));
        return newState;
    }

    public void incrementCacheHits() {
        cacheHits.incrementAndGet();
    }

    public void incrementCacheMisses() {
        cacheMisses.incrementAndGet();
    }

    public long getCacheHits() {
        return cacheHits.get();
    }

    public long getCacheMisses() {
        return cacheMisses.get();
    }

    public void resetStatistics() {
        cacheHits.set(0);
        cacheMisses.set(0);
        LOGGER.info("Cache statistics reset");
    }

    public CacheStats getCurrentStats() {
        CacheStats stats = new CacheStats();
        stats.setLoggingEnabled(loggingEnabled.get());
        stats.setLocalHits(cacheHits.get());
        stats.setLocalMisses(cacheMisses.get());

        try {
            if (entityManager.getDelegate() instanceof EntityManagerImpl emImpl) {
                Session session = emImpl.getServerSession();

                if (session instanceof ServerSession serverSession) {
                    stats.setCacheSize(cacheHits.get() + cacheMisses.get());
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Could not get EclipseLink cache stats: " + e.getMessage());
        }

        return stats;
    }

    public void clearCache() {
        try {
            if (entityManager.getDelegate() instanceof EntityManagerImpl emImpl) {
                Session session = emImpl.getServerSession();

                if (session instanceof ServerSession serverSession) {
                    serverSession.getIdentityMapAccessor().initializeAllIdentityMaps();
                    LOGGER.info("L2 Cache cleared");
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Could not clear cache: " + e.getMessage());
        }
    }

    public static class CacheStats {
        private boolean loggingEnabled;
        private long localHits;
        private long localMisses;
        private long cacheSize;

        public boolean isLoggingEnabled() {
            return loggingEnabled;
        }

        public void setLoggingEnabled(boolean loggingEnabled) {
            this.loggingEnabled = loggingEnabled;
        }

        public long getLocalHits() {
            return localHits;
        }

        public void setLocalHits(long localHits) {
            this.localHits = localHits;
        }

        public long getLocalMisses() {
            return localMisses;
        }

        public void setLocalMisses(long localMisses) {
            this.localMisses = localMisses;
        }

        public long getCacheSize() {
            return cacheSize;
        }

        public void setCacheSize(long cacheSize) {
            this.cacheSize = cacheSize;
        }
    }
}

