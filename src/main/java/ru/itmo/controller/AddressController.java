package ru.itmo.controller;

import ru.itmo.model.Address;
import ru.itmo.model.Organization;
import ru.itmo.model.Worker;
import ru.itmo.service.AddressService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

import static ru.itmo.common.FilterParser.parseFilters;

@Path("/addresses")
@Produces(MediaType.APPLICATION_JSON)
public class AddressController {

    @Inject
    private AddressService addressService;
    @GET
    public List<Address> listPagedFiltered(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("dir") @DefaultValue("asc") String dir,
            @QueryParam("filter") String filter) {
        Map<String, String> filterMap = parseFilters(filter);
        return addressService.findAllAddressesPagedFiltered(page, size, sort, dir, filterMap);
    }
    @GET
    @Path("/recent")
    public List<Address> list() {
        return addressService.findAllAddressesTruncated();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Address address = addressService.findAddressById(id);
        if (address == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(address).build();
    }

    @POST
    public Response create(Address address) {
        try {
            Address created = addressService.createAddress(address);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Address address) {
        try {
            Address updated = addressService.updateAddress(id, address);
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
            addressService.deleteAddress(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage()).build();
        }
    }
}
