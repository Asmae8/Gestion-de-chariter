package com.devbuildv.gestion_chariter.controller;

import com.devbuildv.gestion_chariter.dto.AuthRequest;
import com.devbuildv.gestion_chariter.dto.AuthResponse;
import com.devbuildv.gestion_chariter.dto.RegisterRequest;
import com.devbuildv.gestion_chariter.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.devbuildv.gestion_chariter.security.JwtAuthenticationFilter.AUTH_COOKIE;

@RestController
public class ApiAuthController {

    private final AuthService authService;
    private final boolean secureCookie;

    public ApiAuthController(AuthService authService, @Value("${app.security.secure-cookie:false}") boolean secureCookie) {
        this.authService = authService;
        this.secureCookie = secureCookie;
    }

    @PostMapping({"/api/auth/login", "/api/login"})
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        attachJwtCookie(response, authResponse.token(), false);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping({"/api/auth/register", "/api/register"})
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        attachJwtCookie(response, authResponse.token(), false);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping({"/api/auth/logout", "/api/logout"})
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        attachJwtCookie(response, "", true);
        return ResponseEntity.noContent().build();
    }

    @GetMapping({"/api/auth/me", "/api/me"})
    public ResponseEntity<String> me(Authentication authentication) {
        return ResponseEntity.ok(authentication.getName());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Donnee invalide.")
                .orElse("Donnee invalide.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    private void attachJwtCookie(HttpServletResponse response, String token, boolean expired) {
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE, token)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite("Lax")
                .maxAge(expired ? 0 : 86400)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
