package com.medtrack.application.dto;

// svnr identifies which already-verified identity this finishes registering for - the backend
// re-verifies it against the ID-Austria registry rather than trusting the client's earlier
// verify-identity call.
public record CompleteRegistrationRequest(String svnr, String email, String password) {
}
