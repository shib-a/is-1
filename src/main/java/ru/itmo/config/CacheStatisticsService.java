package ru.itmo.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.identitymaps.IdentityMap;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.server.ServerSession;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
public class CacheStatisticsService {

    private static final Logger LOGGER = Logger.getLogger(CacheStatisticsService.class.getName());

    private final AtomicBoolean loggingEnabled = new AtomicBoolean(false);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong totalOperations = new AtomicLong(0);

    @PersistenceUnit(unitName = "workerManagement")
    private EntityManagerFactory emf;

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

    public void recordCacheHit() {
        cacheHits.incrementAndGet();
        totalOperations.incrementAndGet();
    }

    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
        totalOperations.incrementAndGet();
    }

    public long getCacheHits() {
        try {
            ServerSession serverSession = getServerSession();
            if (serverSession != null) {
                long hits = 0;
                Map<Class<?>, ClassDescriptor> descriptors = serverSession.getDescriptors();
                for (ClassDescriptor descriptor : descriptors.values()) {
                    IdentityMap identityMap = ((AbstractSession) serverSession)
                            .getIdentityMapAccessorInstance()
                            .getIdentityMap(descriptor);
                    if (identityMap != null) {
                        hits += identityMap.getSize();
                    }
                }
                return hits > 0 ? hits : cacheHits.get();
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not get EclipseLink cache hits: " + e.getMessage());
        }
        return cacheHits.get();
    }

    public long getCacheMisses() {
        return cacheMisses.get();
    }

    public void resetStatistics() {
        cacheHits.set(0);
        cacheMisses.set(0);
        totalOperations.set(0);
        LOGGER.info("Cache statistics reset");
    }

    public CacheStats getCurrentStats() {
        CacheStats stats = new CacheStats();
        stats.setLoggingEnabled(loggingEnabled.get());

        try {
            ServerSession serverSession = getServerSession();
            if (serverSession != null) {

                long totalCachedObjects = 0;
                StringBuilder cacheDetails = new StringBuilder();
                Map<Class<?>, ClassDescriptor> descriptors = serverSession.getDescriptors();
                for (Map.Entry<Class<?>, ClassDescriptor> entry : descriptors.entrySet()) {
                    try {
                        IdentityMap identityMap = ((AbstractSession) serverSession)
                                .getIdentityMapAccessorInstance()
                                .getIdentityMap(entry.getValue());
                        if (identityMap != null) {
                            int size = identityMap.getSize();
                            totalCachedObjects += size;
                            if (size > 0) {
                                cacheDetails.append(entry.getKey().getSimpleName())
                                        .append(": ").append(size).append(", ");
                            }
                        }
                    } catch (Exception e) {
                    }
                }

                stats.setLocalHits(totalCachedObjects);
                stats.setLocalMisses(cacheMisses.get());
                stats.setCacheSize(totalCachedObjects);

                String details = cacheDetails.length() > 0
                    ? cacheDetails.substring(0, cacheDetails.length() - 2)
                    : "No cached objects";
                stats.setMessage("L2 Cache active. Cached entities: " + details);
            } else {
                stats.setLocalHits(cacheHits.get());
                stats.setLocalMisses(cacheMisses.get());
                stats.setCacheSize(cacheHits.get() + cacheMisses.get());
                stats.setMessage("Cache statistics from internal counters");
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not get EclipseLink cache stats: " + e.getMessage());
            stats.setLocalHits(cacheHits.get());
            stats.setLocalMisses(cacheMisses.get());
            stats.setCacheSize(cacheHits.get() + cacheMisses.get());
            stats.setMessage("Cache statistics from internal counters (error: " + e.getMessage() + ")");
        }

        return stats;
    }

    public void clearCache() {
        try {
            ServerSession serverSession = getServerSession();
            if (serverSession != null) {
                serverSession.getIdentityMapAccessor().initializeAllIdentityMaps();
                LOGGER.info("L2 Cache cleared");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not clear cache: " + e.getMessage());
        }
    }

    private ServerSession getServerSession() {
        try {
            if (emf != null) {
                if (emf instanceof EntityManagerFactoryImpl emfImpl) {
                    ServerSession session = emfImpl.getServerSession();
                    if (session != null) {
                        return session;
                    }
                }
            }

            if (entityManager != null && entityManager.isOpen()) {
                Object delegate = entityManager.getDelegate();
                if (delegate instanceof EntityManagerImpl emImpl) {
                    Session session = emImpl.getServerSession();
                    if (session instanceof ServerSession serverSession) {
                        return serverSession;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not get server session: " + e.getMessage());
        }
        return null;
    }

    public static class CacheStats {
        private boolean loggingEnabled;
        private long localHits;
        private long localMisses;
        private long cacheSize;
        private String message;

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

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

