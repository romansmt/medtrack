package com.medtrack.api.controller;

import com.medtrack.application.dto.AdminAccessCodeRequest;
import com.medtrack.application.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Demo admin-mode gate - lets the frontend show the patient switcher only to whoever "
        + "knows the access code (see docs/DEVELOPMENT.md). No session or token is issued; the frontend just "
        + "remembers a local flag once this returns success.")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/verify-code")
    @Operation(summary = "Check a claimed admin access code")
    public ResponseEntity<Void> verifyCode(@RequestBody AdminAccessCodeRequest request) {
        if (adminService.verifyAccessCode(request.code())) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
