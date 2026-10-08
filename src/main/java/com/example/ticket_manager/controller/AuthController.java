package com.example.ticket_manager.controller;

import com.example.ticket_manager.exception.ApiException;
import com.example.ticket_manager.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final JwtDecoder jwtDecoder;

    @PostMapping("/login/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body) {
        String googleIdTokenStr = body.get("idToken");

        try {
            Jwt jwt = jwtDecoder.decode(googleIdTokenStr);

            String email = jwt.getClaimAsString("email");
            String name = jwt.getClaimAsString("name");

            String accessToken = jwtService.generateAccessToken(email, name);
            String refreshToken = jwtService.generateRefreshToken(email);

            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "expiresInSeconds", 900
            ));

        } catch (JwtException e) {
            throw new ApiException("Error al validar token de Google: " + e.getMessage(), HttpStatus.UNAUTHORIZED.value());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        if (jwtService.validateToken(refreshToken)) {
            String email = jwtService.getEmailFromToken(refreshToken);

            String newAccessToken = jwtService.generateAccessToken(email, "Usuario");

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "expiresInSeconds", 900
            ));
        }

        throw new ApiException("Refresh Token expirado o inválido. Inicie sesión de nuevo.", HttpStatus.UNAUTHORIZED.value());
    }
}