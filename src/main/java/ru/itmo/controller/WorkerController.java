package ru.itmo.controller;

import ru.itmo.model.Worker;
import ru.itmo.service.WorkerService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.itmo.common.FilterParser.parseFilters;

@Path("/workers")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class WorkerController {
    @Inject
    private WorkerService workerService;

    @GET
    public List<Worker> listPagedFiltered(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("dir") @DefaultValue("asc") String dir,
            @QueryParam("filter") String filter) {
        Map<String, String> filterMap = parseFilters(filter);
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

    @GET
    @Path("/group/salary")
    public Map<Double, Long> nameContains() {
        return workerService.groupBySalary();
    }

    @GET
    @Path("/count/enddate")
    public long countByEndDate(@QueryParam("date") String dateString) {
        LocalDate date = null;
        if (dateString != null && !dateString.isBlank()) {
            try {
                date = LocalDate.parse(dateString);
                return workerService.countByEndDate(Date.from(Instant.from(date)));
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Неверный формат даты");
            }
        }
        return workerService.countByEndDate(null);
    }

    @GET
    @Path("/search/name-contains")
    public List<Worker> nameContains(@QueryParam("q") String q) {
        return workerService.findByNameContaining(q);
    }

    @POST
    @Path("/index-salary/worker/{id}")
    public Response indexWorkerSalary(@PathParam("id") Long id, @QueryParam("coef") double coef) {
        workerService.indexSalaryForWorker(id, coef);
        return Response.ok().build();
    }

    @POST
    @Path("/index-salary/organization/{id}")
    public Response indexOrgSalary(@PathParam("id") Long id, @QueryParam("coef") double coef) {
        workerService.indexSalaryForOrganization(id, coef);
        return Response.ok().build();
    }

}