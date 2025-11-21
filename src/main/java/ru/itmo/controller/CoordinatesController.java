package ru.itmo.controller;

import ru.itmo.model.Coordinates;
import ru.itmo.service.CoordinatesService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/coordinates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoordinatesController {

    @Inject
    private CoordinatesService coordinatesService;

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Coordinates coordinates = coordinatesService.findCoordinatesById(id);
        if (coordinates == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(coordinates).build();
    }

    @POST
    public Response create(Coordinates coordinates) {
        try {
            Coordinates created = coordinatesService.createCoordinates(coordinates);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Coordinates coordinates) {
        try {
            Coordinates updated = coordinatesService.updateCoordinates(id, coordinates);
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
            coordinatesService.deleteCoordinates(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage()).build();
        }
    }
}
