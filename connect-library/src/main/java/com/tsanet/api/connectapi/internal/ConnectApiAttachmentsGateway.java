package com.tsanet.api.connectapi.internal;

import static com.tsanet.api.connectapi.internal.OpenApiMapping.enumValue;

import com.tsanet.api.connectapi.dto.AttachmentConfigDto;
import com.tsanet.api.connectapi.dto.AttachmentForwardResultDto;
import com.tsanet.api.connectapi.dto.CompanyAttachmentConfigDto;
import com.tsanet.api.generated.api.CaseAttachmentsApi;
import com.tsanet.api.generated.model.AttachmentConfigDTO;
import com.tsanet.api.generated.model.AttachmentForwardResultDTO;
import com.tsanet.api.generated.model.CompanyAttachmentConfigDTO;
import com.tsanet.api.storage.AttachmentConfigStorageService;
import com.tsanet.api.storage.AttachmentForwardResultStorageService;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ConnectApiAttachmentsGateway {
    private final CaseAttachmentsApi caseAttachmentsApi;
    private final ConnectApiSessionStore sessionStore;
    private final AttachmentConfigStorageService configStorageService;
    private final AttachmentForwardResultStorageService forwardResultStorageService;

    public ConnectApiAttachmentsGateway(
        CaseAttachmentsApi caseAttachmentsApi,
        ConnectApiSessionStore sessionStore,
        AttachmentConfigStorageService configStorageService,
        AttachmentForwardResultStorageService forwardResultStorageService
    ) {
        this.caseAttachmentsApi = caseAttachmentsApi;
        this.sessionStore = sessionStore;
        this.configStorageService = configStorageService;
        this.forwardResultStorageService = forwardResultStorageService;
    }

    public AttachmentConfigDto getAttachmentConfig(String caseToken) {
        requireLogin();

        AttachmentConfigDTO config = caseAttachmentsApi.getAttachmentConfig(caseToken);
        if (config == null) {
            throw new IllegalStateException("Attachment config returned empty response");
        }
        AttachmentConfigDto dto = toDto(config);
        configStorageService.storeFetched(caseToken, dto);
        return dto;
    }

    public List<AttachmentForwardResultDto> forwardAttachments(String caseToken, String description, List<Path> files) {
        requireLogin();

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one file is required");
        }

        List<File> filePayload = files.stream().map(Path::toFile).toList();
        List<AttachmentForwardResultDTO> results = caseAttachmentsApi.forwardAttachment(caseToken, description, filePayload);
        if (results == null) {
            return Collections.emptyList();
        }
        List<AttachmentForwardResultDto> dtos = results.stream().map(this::toDto).toList();
        forwardResultStorageService.storeForwarded(caseToken, description, dtos);
        return dtos;
    }

    private AttachmentConfigDto toDto(AttachmentConfigDTO config) {
        return new AttachmentConfigDto(
            toCompanyDto(config.getSubmitter()),
            toCompanyDto(config.getReceiver())
        );
    }

    private CompanyAttachmentConfigDto toCompanyDto(CompanyAttachmentConfigDTO company) {
        if (company == null) {
            return null;
        }
        return new CompanyAttachmentConfigDto(company.getCompanyId(), company.getParameters());
    }

    private AttachmentForwardResultDto toDto(AttachmentForwardResultDTO result) {
        return new AttachmentForwardResultDto(
            result.getFileName(),
            enumValue(result.getReceiverStatus()),
            result.getReceiverMessage(),
            enumValue(result.getSubmitterStatus()),
            result.getSubmitterMessage(),
            result.getCompleteSuccess(),
            result.getPartialSuccess()
        );
    }

    private void requireLogin() {
        sessionStore.getBearerToken().orElseThrow(() -> new IllegalStateException("Not logged in"));
    }
}
