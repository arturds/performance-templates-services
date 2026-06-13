package com.example.template.config;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

@ApplicationScoped
@Unremovable
public class OpenApiConfig implements OASFilter {

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        SecurityScheme scheme = OASFactory.createSecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        openAPI.getComponents().addSecurityScheme("bearerAuth", scheme);
        openAPI.addSecurityRequirement(OASFactory.createSecurityRequirement().addScheme("bearerAuth"));
    }
}
