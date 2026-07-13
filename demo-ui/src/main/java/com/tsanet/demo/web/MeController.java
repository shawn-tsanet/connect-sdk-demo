package com.tsanet.demo.web;

import com.tsanet.api.TsaNetApiSession;
import com.tsanet.api.connectapi.dto.UserContextDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    private final TsaNetApiSession session;
    private final SessionGuard guard;

    public MeController(TsaNetApiSession session, SessionGuard guard) {
        this.session = session;
        this.guard = guard;
    }

    @GetMapping("/api/me")
    public UserContextDto me() {
        guard.ensureAuthenticated();
        return session.users().getCurrentUser();
    }
}
