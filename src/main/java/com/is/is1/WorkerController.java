package com.is.is1;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/workers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON})
public class WorkerController {
    @Inject
    private WorkerService workerService;
    @Path("/add")
    @POST
    public Response addWorker(WorkerDTO workerDTO){

        return Response.ok().build();
    }

    @Path("/setOrganization")
}
