package com.medtrack.domain;

import java.time.Instant;

public record EHealthCardSession(String svnr, Instant establishedAt) {
}
