package com.is.is1;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Set;

//@ApplicationPath("/")?
public class JaxRsActivator extends Application {
    @Override
    public Set<Object> getSingletons() {
        return Set.of(new JacksonJsonProvider());
    }
}
