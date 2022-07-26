package com.cloudSavvy.validator;

import software.amazon.awssdk.utils.StringUtils;

import java.util.regex.Pattern;

public class EmailValidator {

    public static final String EMAIL_SEPARATOR = ",";
    private static final String EMAIL_PATTERN = "^(?=.{1,64}@)[\\p{L}\\d_-]+(\\.[\\p{L}\\d_-]+)*@"
            + "[^-][\\p{L}\\d-]+(\\.[\\p{L}\\d-]+)*(\\.\\p{L}{2,})$";
    public static final String EMAIL_ERROR_TEMPLATE =
            "Invalid email is provided:%s. Sample format=user1@testdomain.com,user2@testdomain.com";

    public static void validateFromEmail(String fromEmailAddress) {
        // If email is not provided, no email will be sent
        if (StringUtils.isEmpty(fromEmailAddress)) {
            return;
        }

        if (!EmailValidator.validateEmail(fromEmailAddress)) {
            String message = String.format("Invalid email is provided:%s", fromEmailAddress);
            throw new IllegalArgumentException(message);
        }
    }

    public static void validateToEmailAddresses(String toEmailAddresses) {
        // If to email is not provided, from email will be used
        if (StringUtils.isEmpty(toEmailAddresses)) {
            return;
        }

        for (String email : toEmailAddresses.split(EMAIL_SEPARATOR)) {
            if (!EmailValidator.validateEmail(email)) {
                String message = String.format(EMAIL_ERROR_TEMPLATE, email);
                throw new IllegalArgumentException(message);
            }
        }
    }

    public static boolean validateEmail(String emailAddress) {
        return Pattern.compile(EMAIL_PATTERN)
                .matcher(emailAddress)
                .matches();
    }
}
