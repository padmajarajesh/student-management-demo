package com.demo.student.dto;

/** Validation patterns shared by several request DTOs. */
public final class ValidationPatterns {

    /** Optional leading +, then 7-20 digits, spaces, dashes or brackets. */
    public static final String PHONE = "^\\+?[0-9][0-9 ()\\-]{6,19}$";
    public static final String PHONE_MESSAGE = "Phone number must be 7-20 digits and may start with +";

    public static final String GENDER = "^(Male|Female|Other)$";
    public static final String GENDER_MESSAGE = "Gender must be Male, Female or Other";

    private ValidationPatterns() {
    }
}
