package com.medtrack.infrastructure.admin;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockAdminAuthAdapterTest {

    private final MockAdminAuthAdapter adapter = new MockAdminAuthAdapter();

    @Test
    void acceptsTheDocumentedAccessCode() {
        assertThat(adapter.verifyAccessCode(MockAdminAuthAdapter.ACCESS_CODE)).isTrue();
    }

    @Test
    void rejectsAnyOtherCode() {
        assertThat(adapter.verifyAccessCode("wrong-code")).isFalse();
        assertThat(adapter.verifyAccessCode("")).isFalse();
    }
}
