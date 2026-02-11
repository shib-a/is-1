package ru.itmo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.itmo.DTO.ImportHistoryDTO;
import ru.itmo.DTO.WorkerDTO;
import ru.itmo.config.MinioService;
import ru.itmo.service.ImportService;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/import")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ImportController {

    @Inject
    private ImportService importService;

    @POST
    @Path("/workers")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response importWorkers(InputStream inputStream) {

        try {
            byte[] fileContent = inputStream.readAllBytes();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            WorkerDTO[] workerArray = objectMapper.readValue(fileContent, WorkerDTO[].class);
            List<WorkerDTO> workers = Arrays.asList(workerArray);

            if (workers.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"No workers in file\"}").build();
            }

            ImportHistoryDTO history = importService.importWorkers(workers, fileContent);

            return Response.ok(history).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Import failed: " + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/history")
    public Response getImportHistory(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int pageSize,
            @QueryParam("sort") @DefaultValue("timestamp") String sortField,
            @QueryParam("dir") @DefaultValue("desc") String sortDirection) {
        try {
            List<ImportHistoryDTO> history = importService.getImportHistory(page, pageSize, sortField, sortDirection);
            long total = importService.getImportHistoryCount();

            Map<String, Object> response = new HashMap<>();
            response.put("data", history);
            response.put("total", total);
            response.put("page", page);
            response.put("pageSize", pageSize);

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Failed to retrieve history: " + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/download/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("fileName") String fileName) {
        try {
            byte[] fileContent = importService.downloadImportFile(fileName);
            return Response.ok(fileContent)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (MinioService.MinioException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"File not found: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
