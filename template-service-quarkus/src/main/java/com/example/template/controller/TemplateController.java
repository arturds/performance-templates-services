package com.example.template.controller;

import com.example.template.client.GoogleClient;
import com.example.template.domain.dto.PageResponse;
import com.example.template.domain.dto.TemplateDTO;
import com.example.template.service.TemplateService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/template")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "bearerAuth")
public class TemplateController {

    @Inject
    TemplateService templateService;

    @Inject
    @RestClient
    GoogleClient googleClient;

    @GET
    @Path("/google")
    public Response getGoogle() {
        return Response.ok(googleClient.getGoogleHomePage()).build();
    }

    @POST
    @Path("/")
    @Operation(description = "Create template")
    @APIResponse(responseCode = "201", description = "Template created")
    @APIResponse(responseCode = "422", description = "Template already exist")
    public Response createTemplate(TemplateDTO templateDTO) {
        return templateService.createTemplate(templateDTO);
    }

    @PUT
    @Path("/{id}")
    @Operation(description = "Update template")
    @APIResponse(responseCode = "200", description = "Template updated")
    @APIResponse(responseCode = "404", description = "Template not found")
    public Response updateTemplate(@PathParam("id") Long id, TemplateDTO templateDTO) {
        return templateService.updateTemplate(id, templateDTO);
    }

    @GET
    @Path("/{id}")
    @Operation(description = "Get template")
    @APIResponse(responseCode = "200", description = "Template found")
    @APIResponse(responseCode = "404", description = "Template not found")
    public Response getTemplate(@PathParam("id") Long id) {
        return templateService.getTemplate(id);
    }

    @GET
    @Operation(description = "Get paginated templates")
    @APIResponse(responseCode = "200", description = "Templates found")
    public Response getTemplates(
            @QueryParam("page") @DefaultValue("1") @Positive int page,
            @QueryParam("pageSize") @DefaultValue("10") @Min(10) @Max(100) int pageSize,
            @QueryParam("sort") @DefaultValue("asc") String sort,
            @QueryParam("sortField") @DefaultValue("id") String sortField) {

        return templateService.getTemplates(page, pageSize);
    }

    @DELETE
    @Path("/{id}")
    @Operation(description = "Soft delete template")
    @APIResponse(responseCode = "200", description = "Template deleted")
    public Response deleteTemplate(@PathParam("id") Long id) {
        return templateService.deleteTemplate(id);
    }
}
