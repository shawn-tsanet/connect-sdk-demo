package com.tsanet.api.facade;

import com.tsanet.api.connectapi.dto.AttachmentConfigDto;
import com.tsanet.api.connectapi.dto.AttachmentForwardResultDto;
import com.tsanet.api.connectapi.dto.StoredAttachmentConfigDto;
import com.tsanet.api.connectapi.dto.StoredAttachmentForwardResultDto;
import java.nio.file.Path;
import java.util.List;

public interface AttachmentsFacade {
    AttachmentConfigDto getAttachmentConfig(String caseToken);

    List<AttachmentForwardResultDto> forwardAttachments(String caseToken, String description, List<Path> files);

    List<StoredAttachmentConfigDto> listStoredAttachmentConfigs();

    List<StoredAttachmentConfigDto> listStoredAttachmentConfigsForRequest(String caseToken);

    List<StoredAttachmentForwardResultDto> listStoredForwardResults();

    List<StoredAttachmentForwardResultDto> listStoredForwardResultsForRequest(String caseToken);
}
