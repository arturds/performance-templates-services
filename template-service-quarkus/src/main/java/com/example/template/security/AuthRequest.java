package com.example.template.security;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AuthRequest(String username, String password) {
}
