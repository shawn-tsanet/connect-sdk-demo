package com.tsanet.clientdemo.connectapi.dto;

public record UserContextDto(
    Long companyId,
    String companyName,
    Long userId,
    String username,
    String email,
    String firstName,
    String lastName
) {
}
