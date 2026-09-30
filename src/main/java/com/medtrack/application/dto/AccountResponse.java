package com.medtrack.application.dto;

// A lightweight summary of a logged-in/just-registered MedTrack account - deliberately not the full
// ECardDetailsResponse shape, since card-serial/carrier/expiry data isn't relevant once you're past
// identity verification (the frontend only ever needs the svnr to select the active patient).
public record AccountResponse(String svnr, String medtrackId, String name, String email) {
}
