package com.example.template.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    JwtService jwtService;

    @Inject
    PasswordService passwordService;

    @POST
    @Path("/login")
    public Response login(AuthRequest authRequest) {
        if (authRequest == null
                || !passwordService.authenticate(authRequest.username(), authRequest.password())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Invalid credentials"))
                    .build();
        }

        String token = jwtService.generateToken(authRequest.username());
        return Response.ok(new TokenResponse(token)).build();
    }
}
