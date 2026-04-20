package com.auth.jwt_api.dtos;

public record LoginResponseDTO(String token, String refreshToken) {
}
