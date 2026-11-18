package com.demo.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Used for both creating and updating a student. */
public record StudentRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = ValidationPatterns.PHONE, message = ValidationPatterns.PHONE_MESSAGE)
        String phoneNumber,

        @NotBlank(message = "Department is required")
        @Size(max = 100, message = "Department must be at most 100 characters")
        String department,

        @NotBlank(message = "Gender is required")
        @Pattern(regexp = ValidationPatterns.GENDER, message = ValidationPatterns.GENDER_MESSAGE)
        String gender,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Size(max = 255, message = "Address must be at most 255 characters")
        String address
) {
}
