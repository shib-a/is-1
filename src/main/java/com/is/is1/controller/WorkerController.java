package com.is.is1.controller;

import com.is.is1.common.Page;
import com.is.is1.model.Worker;
import com.is.is1.service.WorkerService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/workers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON})
@ApplicationScoped
public class WorkerController {
    @Inject
    private WorkerService workerService;

    @GET
    public Page<Worker> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("dir") @DefaultValue("asc") String dir,
            @QueryParam("filter") String filter) {
        return workerService.findAllPaged(page, size, sort, dir, filter);
    }
    // Получение одного работника по id
    @GET
    @Path("/get")
    public Worker get(@QueryParam("id") @NotNull Long id) {
        Worker w = workerService.findById(id);
        if (w == null) throw new NotFoundException("Worker not found");
        return w;
    }

    // Создание — без id
    @POST
    public Response create(@Valid Worker worker) {
        Worker created = workerService.create(worker);
        return Response.ok().entity(created).build();
    }

    // Обновление по id из query
    @PUT
    @Path("/update")
    public Worker update(
            @QueryParam("id") @NotNull Long id,
            @Valid Worker worker) {
        return workerService.update(id, worker);
    }

    // Удаление по id из query
    @DELETE
    @Path("/delete")
    public Response delete(@QueryParam("id") @NotNull Long id) {
        workerService.delete(id);
        return Response.noContent().build();
    }

    // ==================== Специальные операции ====================

    @GET
    @Path("/search/name-contains")
    public List<Worker> nameContains(@QueryParam("q") String q) {
        if (q == null || q.isBlank()) throw new BadRequestException("Parameter q is required");
        return workerService.nameContains(q);
    }

    @GET
    @Path("/search/name-starts")
    public List<Worker> nameStartsWith(@QueryParam("q") String q) {
        if (q == null || q.isBlank()) throw new BadRequestException("Parameter q is required");
        return workerService.nameStartsWith(q);
    }

    @GET
    @Path("/search/rating-less")
    public List<Worker> ratingLessThan(@QueryParam("value") @NotNull Double value) {
        return workerService.ratingLessThan(value);
    }

    // Принять на работу — только query-параметры
    @POST
    @Path("/hire")
    public Response hire(
            @QueryParam("workerId") @NotNull Long workerId,
            @QueryParam("orgId")   @NotNull Long orgId) {
        workerService.hire(workerId, orgId);
        return Response.ok().build();
    }

    // Переместить сотрудника — только query-параметры
    @POST
    @Path("/transfer")
    public Response transfer(
            @QueryParam("workerId") @NotNull Long workerId,
            @QueryParam("newOrgId") @NotNull Long newOrgId) {
        workerService.transfer(workerId, newOrgId);
        return Response.ok().build();
    }
}