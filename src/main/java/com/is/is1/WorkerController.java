package com.is.is1;

import com.is.is1.DTOs.GetRequestDTO;
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
    public Response addWorker(Worker workerDTO){
        workerService.save(workerDTO);
        return Response.ok().build();
    }

    @Path("/getAll")
    @GET
    public Response getAllWorkers(GetRequestDTO getRequestDTO){
        return Response.ok().entity(workerService.findAllPaged(getRequestDTO.getPageNumber(), getRequestDTO.getPageSize())).build();
    }
    @Path("/getTotalPages")
    @GET
    public Response getTotalPages(){
        return Response.ok().entity(workerService.count()).build();
    }
}
