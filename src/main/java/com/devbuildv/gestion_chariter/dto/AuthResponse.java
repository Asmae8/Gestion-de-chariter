package com.devbuildv.gestion_chariter.dto;

public record AuthResponse(String token, String tokenType, String email, String role) {
}
