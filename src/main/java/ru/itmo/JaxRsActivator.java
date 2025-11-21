package ru.itmo;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class JaxRsActivator extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        resources.add(ru.itmo.controller.WorkerController.class);
        resources.add(ru.itmo.controller.OrganizationController.class);
        resources.add(ru.itmo.controller.CoordinatesController.class);
        resources.add(ru.itmo.controller.AddressController.class);
        resources.add(ru.itmo.controller.LocationController.class);
        resources.add(ru.itmo.controller.PersonController.class);
        return resources;
    }
}