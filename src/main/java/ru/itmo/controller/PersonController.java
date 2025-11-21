package ru.itmo.controller;

import ru.itmo.model.Person;
import ru.itmo.service.PersonService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;

@Path("/api/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class PersonController {
    @Inject
    PersonService personService;

    @GET
    public List<Person> list() {
        return personService.findAll();
    }

    @GET
    @Path("/get")
    public Person get(@QueryParam("id") @NotNull Long id) {
        Person person = personService.findById(id);
        if (person == null) throw new NotFoundException("Person not found");
        return person;
    }

    @POST
    public Response create(@Valid Person person, @Context UriInfo uriInfo) {
        Person created = personService.create(person);
        return Response.ok().entity(created).build();
    }

    @PUT
    @Path("/update")
    public Person update(
            @QueryParam("id") @NotNull Long id,
            @Valid Person person) {
        return personService.update(id, person);
    }

    @DELETE
    @Path("/delete")
    public Response delete(@QueryParam("id") @NotNull Long id) {
        personService.delete(id);
        return Response.noContent().build();
    }
}
