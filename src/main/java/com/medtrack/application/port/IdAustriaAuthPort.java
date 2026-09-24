package com.medtrack.application.port;

import com.medtrack.domain.IdAustriaIdentity;

import java.time.LocalDate;

public interface IdAustriaAuthPort {

    IdAustriaIdentity authenticate(String fullName, LocalDate dateOfBirth);
}
