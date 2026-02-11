package ru.itmo.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.itmo.config.CacheStatisticsService;

import java.util.HashMap;
import java.util.Map;

/**
 * REST контроллер для управления L2 JPA Cache.
 * Позволяет включать/отключать логирование статистики кеша.
 */
@Path("/cache")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CacheController {

    @Inject
    private CacheStatisticsService cacheStatisticsService;

    /**
     * Получить текущую статистику кеша
     */
    @GET
    @Path("/stats")
    public Response getStats() {
        CacheStatisticsService.CacheStats stats = cacheStatisticsService.getCurrentStats();
        return Response.ok(stats).build();
    }

    /**
     * Включить логирование статистики кеша
     */
    @POST
    @Path("/logging/enable")
    public Response enableLogging() {
        cacheStatisticsService.enableLogging();
        Map<String, Object> response = new HashMap<>();
        response.put("loggingEnabled", true);
        response.put("message", "Cache logging enabled");
        return Response.ok(response).build();
    }

    /**
     * Отключить логирование статистики кеша
     */
    @POST
    @Path("/logging/disable")
    public Response disableLogging() {
        cacheStatisticsService.disableLogging();
        Map<String, Object> response = new HashMap<>();
        response.put("loggingEnabled", false);
        response.put("message", "Cache logging disabled");
        return Response.ok(response).build();
    }

    /**
     * Переключить состояние логирования
     */
    @POST
    @Path("/logging/toggle")
    public Response toggleLogging() {
        boolean newState = cacheStatisticsService.toggleLogging();
        Map<String, Object> response = new HashMap<>();
        response.put("loggingEnabled", newState);
        response.put("message", "Cache logging " + (newState ? "enabled" : "disabled"));
        return Response.ok(response).build();
    }

    /**
     * Очистить L2 кеш
     */
    @POST
    @Path("/clear")
    public Response clearCache() {
        cacheStatisticsService.clearCache();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "L2 Cache cleared");
        return Response.ok(response).build();
    }

    /**
     * Сбросить статистику
     */
    @POST
    @Path("/stats/reset")
    public Response resetStats() {
        cacheStatisticsService.resetStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cache statistics reset");
        return Response.ok(response).build();
    }
}

