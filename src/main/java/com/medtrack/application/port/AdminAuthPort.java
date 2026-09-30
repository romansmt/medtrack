package com.medtrack.application.port;

public interface AdminAuthPort {

    boolean verifyAccessCode(String code);
}
