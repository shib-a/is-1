package ru.itmo.controller;

import ru.itmo.common.Page;
import ru.itmo.model.Worker;
import ru.itmo.service.WorkerService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/workers")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class WorkerController {
    @Inject
    private WorkerService workerService;

    @GET
    @Path("/list")
    public List<Worker> listPagedFiltered(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("dir") @DefaultValue("asc") String dir,
            @QueryParam("filters") String filters) {
        Map<String, String> filterMap = parseFilters(filters);
        return workerService.findAllWorkersPagedFiltered(page, size, sort, dir, filterMap);
    }
    // Получение одного работника по id
    @GET
    @Path("/{id}")
    public Worker get(@PathParam("id") @NotNull Long id) {
        Worker w = workerService.findWorkerById(id);
        if (w == null) throw new NotFoundException("Worker not found");
        return w;
    }

    @POST
    public Response create(@Valid Worker worker) {
        Worker created = workerService.createWorker(worker);
        return Response.ok().entity(created).build();
    }

    // Update a worker by ID
    @PUT
    @Path("/{id}")
    public Worker update(
            @PathParam("id") @NotNull Long id,
            @Valid Worker worker) {
        return workerService.updateWorker(id, worker);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") @NotNull Long id) {
        workerService.deleteWorker(id);
        return Response.noContent().build();
    }

    // ==================== Специальные операции ====================

    @GET
    @Path("/search/name-contains/{q}")
    public List<Worker> nameContains(@PathParam("q") String q) {
        if (q == null || q.isBlank()) throw new BadRequestException("Parameter q is required");
        return workerService.nameContains(q);
    }

    @GET
    @Path("/search/name-starts/{q}")
    public List<Worker> nameStartsWith(@PathParam("q") String q) {
        if (q == null || q.isBlank()) throw new BadRequestException("Parameter q is required");
        return workerService.nameStartsWith(q);
    }

    @GET
    @Path("/search/rating-less/{value}")
    public List<Worker> ratingLessThan(@PathParam("value") @NotNull Double value) {
        return workerService.ratingLessThan(value);
    }

    // Принять на работу — только query-параметры
    @POST
    @Path("/hire/{workerId}/{orgId}")
    public Response hire(
            @PathParam("workerId") @NotNull Long workerId,
            @PathParam("orgId") @NotNull Long orgId) {
        workerService.hire(workerId, orgId);
        return Response.ok().build();
    }

    // Переместить сотрудника — только query-параметры
    @POST
    @Path("/transfer/{workerId}/{newOrgId}")
    public Response transfer(
            @PathParam("workerId") @NotNull Long workerId,
            @PathParam("newOrgId") @NotNull Long newOrgId) {
        workerService.transfer(workerId, newOrgId);
        return Response.ok().build();
    }

    private Map<String, String> parseFilters(String filters) {
        Map<String, String> filterMap = new HashMap<>();
        if (filters != null && !filters.isBlank()) {
            String[] pairs = filters.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    filterMap.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return filterMap;
    }
}