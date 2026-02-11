package ru.itmo.controller;

import jakarta.persistence.NoResultException;
import ru.itmo.model.Organization;
import ru.itmo.model.Worker;
import ru.itmo.service.OrganizationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;
import java.util.Map;

import static ru.itmo.common.FilterParser.parseFilters;

@Path("/organizations")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class OrganizationController {
    @Inject
    private OrganizationService organizationService;

    @GET
    public List<Organization> listPagedFiltered(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("dir") @DefaultValue("asc") String dir,
            @QueryParam("filter") String filter) {
        Map<String, String> filterMap = parseFilters(filter);
        return organizationService.findAllOrganizationsPagedFiltered(page, size, sort, dir, filterMap);
    }

    @GET
    @Path("/recent")
    public List<Organization> list() {
        return organizationService.findAllOrganizationsTruncated();
    }

    @GET
    @Path("/{id}")
    public Organization get(@PathParam("id") @NotNull Long id) {
        Organization org = organizationService.findOrganizationById(id);
        if (org == null) throw new NotFoundException("Organization not found");
        return org;
    }

    @POST
    public Response create(@Valid Organization org) {
        Organization created = organizationService.createOrganization(org);
        return Response.ok().entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Organization update(
            @PathParam("id") @NotNull Long id,
            @Valid Organization org) {
        return organizationService.updateOrganization(id, org);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(
            @PathParam("id") @NotNull Long id,
            @QueryParam("newOrgId") Long newOrgId) {
        try {
            if (newOrgId != null) {
                organizationService.deleteWithReassign(id, newOrgId);
            } else {
                organizationService.deleteOrganization(id);
            }
            return Response.noContent().build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (NoResultException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Organization not found\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
