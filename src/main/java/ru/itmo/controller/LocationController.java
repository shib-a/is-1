package ru.itmo.controller;

import ru.itmo.model.Location;
import ru.itmo.model.Organization;
import ru.itmo.service.LocationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/locations")
@Produces(MediaType.APPLICATION_JSON)
public class LocationController {

    @Inject
    private LocationService locationService;
    @GET
    @Path("/recent")
    public List<Location> list() {
        return locationService.findAllLocationsTruncated();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Location location = locationService.findLocationById(id);
        if (location == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(location).build();
    }

    @POST
    public Response create(Location location) {
        try {
            Location created = locationService.createLocation(location);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Location location) {
        try {
            Location updated = locationService.updateLocation(id, location);
            return Response.ok(updated).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        try {
            locationService.deleteLocation(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage()).build();
        }
    }
}
