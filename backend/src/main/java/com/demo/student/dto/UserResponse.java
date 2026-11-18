package com.demo.student.dto;

import com.demo.student.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/** Public view of a user. The password hash is deliberately not included. */
public record UserResponse(UUID id, String fullName, String email, String phoneNumber, OffsetDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCreatedAt().atOffset(ZoneOffset.UTC));
    }
}
