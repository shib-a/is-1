package ru.itmo.controller;

import ru.itmo.DTO.WorkerDTO;
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
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;

import static ru.itmo.common.FilterParser.parseFilters;

@Path("/workers")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class WorkerController {
    @Inject
    private WorkerService workerService;

    @GET
    public List<WorkerDTO> listPagedFiltered(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("dir") @DefaultValue("asc") String dir,
            @QueryParam("filter") String filter) {
        Map<String, String> filterMap = parseFilters(filter);
        return workerService.findAllWorkersPagedFiltered(page, size, sort, dir, filterMap);
    }

    @GET
    @Path("/{id}")
    public WorkerDTO get(@PathParam("id") @NotNull Long id) {
        WorkerDTO w = workerService.findWorkerById(id);
        if (w == null) throw new NotFoundException("Worker not found");
        return w;
    }

    @POST
    public Response create(@Valid WorkerDTO worker) {
        System.out.println("Creating worker: " + worker.getName());
        WorkerDTO created = workerService.createWorker(worker);
        System.out.println("Created worker: " + worker.getName());
        return Response.ok().entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public WorkerDTO update(
            @PathParam("id") @NotNull Long id,
            @Valid WorkerDTO worker) {
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
                Instant instant = date.atStartOfDay(ZoneOffset.UTC).toInstant();
                return workerService.countByEndDate(Date.from(instant));
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