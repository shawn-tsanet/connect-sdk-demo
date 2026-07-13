package com.tsanet.demo.web;

import com.tsanet.api.TsaNetApiSession;
import com.tsanet.api.connectapi.dto.AttachmentConfigDto;
import com.tsanet.api.connectapi.dto.AttachmentForwardResultDto;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AttachmentsController {

    private final TsaNetApiSession session;
    private final SessionGuard guard;

    public AttachmentsController(TsaNetApiSession session, SessionGuard guard) {
        this.session = session;
        this.guard = guard;
    }

    @GetMapping("/api/requests/{token}/attachments/config")
    public AttachmentConfigDto getConfig(@PathVariable String token) {
        guard.ensureAuthenticated();
        return session.attachments().getAttachmentConfig(token);
    }

    @PostMapping("/api/requests/{token}/attachments")
    public List<AttachmentForwardResultDto> forward(
        @PathVariable String token,
        @RequestParam("description") String description,
        @RequestParam("files") List<MultipartFile> files
    ) {
        guard.ensureAuthenticated();
        List<Path> tempFiles = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "attachment";
                Path temp = Files.createTempDirectory("tsanet-demo-attach").resolve(original);
                file.transferTo(temp);
                tempFiles.add(temp);
            }
            return session.attachments().forwardAttachments(token, description, tempFiles);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            for (Path temp : tempFiles) {
                try {
                    Files.deleteIfExists(temp);
                    Files.deleteIfExists(temp.getParent());
                } catch (IOException ignored) {
                    // best-effort temp cleanup
                }
            }
        }
    }
}
