package ru.itmo.controller;

import ru.itmo.model.Organization;
import ru.itmo.service.OrganizationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;
@Path("/api/organizations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class OrganizationController {
    @Inject
    OrganizationService organizationService;

    // Список всех организаций (для выпадающего списка при создании работника)
    @GET
    public List<Organization> list() {
        return organizationService.findAll();
    }

    @GET
    @Path("/get")
    public Organization get(@QueryParam("id") @NotNull Long id) {
        Organization org = organizationService.findById(id);
        if (org == null) throw new NotFoundException("Organization not found");
        return org;
    }

    @POST
    public Response create(@Valid Organization org) {
        Organization created = organizationService.create(org);
        return Response.ok().entity(created).build();
    }

    @PUT
    @Path("/update")
    public Organization update(
            @QueryParam("id") @NotNull Long id,
            @Valid Organization org) {
        return organizationService.update(id, org);
    }

    @DELETE
    @Path("/delete")
    public Response delete(
            @QueryParam("id") @NotNull Long id,
            @QueryParam("newOrgId") @NotNull Long newOrgId) {
        organizationService.deleteWithReassign(id, newOrgId);
        return Response.noContent().build();
    }
}
