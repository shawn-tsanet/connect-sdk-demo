package com.tsanet.clientdemo.connectapi.dto;

public record CollaborationRequestFormDto(
    long receiverCompanyId,
    long documentId,
    int customFieldCount
) {
}
