package com.medtrack.application.port;

import com.medtrack.domain.EHealthCardSession;

public interface EHealthCardPort {

    EHealthCardSession lookupBySvnr(String svnr);
}
