package com.example.template.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "google-client")
@Produces(MediaType.TEXT_HTML)
public interface GoogleClient {

    @GET
    String getGoogleHomePage();
}
