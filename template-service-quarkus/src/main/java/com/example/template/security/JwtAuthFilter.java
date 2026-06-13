package com.example.template.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.Set;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/auth/login",
            "/v3/api-docs",
            "/swagger-ui",
            "/q/openapi",
            "/q/swagger-ui",
            "/actuator/health",
            "/q/health"
    );

    @Inject
    JwtService jwtService;

    @Inject
    PasswordService passwordService;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (isPublicPath(path)) {
            return;
        }

        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        String username = jwtService.extractUsername(token);
        if (username == null || !passwordService.userExists(username)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    private boolean isPublicPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        return PUBLIC_PATHS.stream().anyMatch(publicPath ->
                normalized.equals(publicPath) || normalized.startsWith(publicPath + "/"));
    }
}
