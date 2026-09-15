package com.sonrisa.homework.auth;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RequestingUserTest {

    @Test
    void adminCanAccessAnyOwner() {
        RequestingUser admin = new RequestingUser(UUID.randomUUID(), true);
        assertThat(admin.canAccess(UUID.randomUUID())).isTrue();
    }

    @Test
    void regularUserCanAccessOwnResources() {
        UUID id = UUID.randomUUID();
        RequestingUser user = new RequestingUser(id, false);
        assertThat(user.canAccess(id)).isTrue();
    }

    @Test
    void regularUserCannotAccessSomeoneElsesResources() {
        RequestingUser user = new RequestingUser(UUID.randomUUID(), false);
        assertThat(user.canAccess(UUID.randomUUID())).isFalse();
    }
}
