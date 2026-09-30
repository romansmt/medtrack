package com.medtrack.infrastructure.admin;

import com.medtrack.application.port.AdminAuthPort;
import org.springframework.stereotype.Component;

// One hardcoded access code, same "fake, nothing real to check against" spirit as every other mock
// adapter in this project (MockIdAustriaAuthAdapter always succeeds; standard login never checks a
// password). Deliberately documented in full in docs/DEVELOPMENT.md - this is not a real secret, it
// gates a demo-only UI convenience (showing the patient switcher), not any actual data access: every
// patient's data is already reachable directly through the API regardless of this check.
@Component
public class MockAdminAuthAdapter implements AdminAuthPort {

    static final String ACCESS_CODE = "MEDTRACK-ADMIN-2026";

    @Override
    public boolean verifyAccessCode(String code) {
        return ACCESS_CODE.equals(code);
    }
}
