package com.tsanet.api.connectapi.dto;

public record CollaborationRequestFormDto(
    long receiverCompanyId,
    long documentId,
    int customFieldCount
) {
}
