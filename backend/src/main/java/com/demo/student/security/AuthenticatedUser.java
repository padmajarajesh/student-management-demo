package com.demo.student.security;

import java.util.UUID;

/**
 * The principal stored in the SecurityContext for an authenticated request.
 * Controllers receive it with {@code @AuthenticationPrincipal AuthenticatedUser user}.
 */
public record AuthenticatedUser(UUID id, String email) {
}
