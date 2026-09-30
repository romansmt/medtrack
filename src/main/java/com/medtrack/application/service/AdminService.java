package com.medtrack.application.service;

import com.medtrack.application.port.AdminAuthPort;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminAuthPort adminAuthPort;

    public AdminService(AdminAuthPort adminAuthPort) {
        this.adminAuthPort = adminAuthPort;
    }

    public boolean verifyAccessCode(String code) {
        return adminAuthPort.verifyAccessCode(code);
    }
}
