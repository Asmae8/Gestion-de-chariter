package com.devbuildv.gestion_chariter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email est invalide")
        String email,
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
        String password,
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom est trop long")
        String nom,
        @NotBlank(message = "Le prenom est obligatoire")
        @Size(max = 100, message = "Le prenom est trop long")
        String prenom,
        @Pattern(
                regexp = "^$|^[0-9+()\\-\\s]{8,20}$",
                message = "Le telephone est invalide"
        )
        String telephone
) {
}
