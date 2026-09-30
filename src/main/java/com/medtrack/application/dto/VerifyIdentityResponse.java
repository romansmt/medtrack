package com.medtrack.application.dto;

// alreadyRegistered tells the frontend which step to show next: the register tab shows an
// "already exists, please log in" alert when true, or proceeds to the create-credentials step when
// false; the login tab does the opposite (logs in directly when true, tells the user to register
// first when false).
public record VerifyIdentityResponse(ECardDetailsResponse verified, boolean alreadyRegistered) {
}
