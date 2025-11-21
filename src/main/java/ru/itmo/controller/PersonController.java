package ru.itmo.controller;

import ru.itmo.model.Organization;
import ru.itmo.model.Person;
import ru.itmo.model.Worker;
import ru.itmo.service.PersonService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
import java.util.Map;

import static ru.itmo.common.FilterParser.parseFilters;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class PersonController {
    @Inject
    private PersonService personService;

    @GET
    public List<Person> listPagedFiltered(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("dir") @DefaultValue("asc") String dir,
            @QueryParam("filter") String filter) {
        Map<String, String> filterMap = parseFilters(filter);
        return personService.findAllPersonsPagedFiltered(page, size, sort, dir, filterMap);
    }

    @GET
    @Path("/recent")
    public List<Person> list() {
        return personService.findAllPersonsTruncated();
    }

    @GET
    @Path("/get")
    public Person get(@QueryParam("id") @NotNull Long id) {
        Person person = personService.findPersonById(id);
        if (person == null) throw new NotFoundException("Person not found");
        return person;
    }

    @POST
    public Response create(@Valid Person person, @Context UriInfo uriInfo) {
        Person created = personService.createPerson(person);
        return Response.ok().entity(created).build();
    }

    @PUT
    @Path("/update")
    public Person update(
            @QueryParam("id") @NotNull Long id,
            @Valid Person person) {
        return personService.updatePerson(id, person);
    }

    @DELETE
    @Path("/delete")
    public Response delete(@QueryParam("id") @NotNull Long id) {
        personService.deletePerson(id);
        return Response.noContent().build();
    }
}
