package com.demo.student.dto;

import com.demo.student.entity.Student;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record StudentResponse(
        UUID id,
        String fullName,
        String email,
        String phoneNumber,
        String department,
        String gender,
        LocalDate dateOfBirth,
        String address,
        OffsetDateTime createdAt
) {

    public static StudentResponse from(Student s) {
        return new StudentResponse(
                s.getId(),
                s.getFullName(),
                s.getEmail(),
                s.getPhoneNumber(),
                s.getDepartment(),
                s.getGender(),
                s.getDateOfBirth(),
                s.getAddress(),
                s.getCreatedAt().atOffset(ZoneOffset.UTC));
    }
}
