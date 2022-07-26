package com.cloudSavvy.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmailValidatorTest {

    @Test
    public void test_validateEmail() {
        assertTrue(EmailValidator.validateEmail("a@gmail.com"));
        assertTrue(EmailValidator.validateEmail("aB.cd@gmail.com"));
        assertTrue(EmailValidator.validateEmail("b2@gmail.com"));
        assertFalse(EmailValidator.validateEmail("b2gmail.com"));
        assertFalse(EmailValidator.validateEmail("b2@gmail.com."));
        assertFalse(EmailValidator.validateEmail("b2.@gmail.com"));
        assertTrue(EmailValidator.validateEmail("b2.t@gmail.com"));
    }

    @Test
    public void test_validateFromEmail() {
        EmailValidator.validateFromEmail("user1@testdomain.com");
        assertThrows(IllegalArgumentException.class, () ->
                EmailValidator.validateFromEmail("b2gmail.com"));
        assertThrows(IllegalArgumentException.class, () ->
                EmailValidator.validateFromEmail("user1@testdomain.com,user2@testdomain.com"));
    }

    @Test
    public void test_validateToEmailAddresses() {
        EmailValidator.validateToEmailAddresses("user1@testdomain.com");
        EmailValidator.validateToEmailAddresses("user1@testdomain.com,user2@testdomain.com");
        assertThrows(IllegalArgumentException.class, () ->
                EmailValidator.validateToEmailAddresses("b2gmail.com"));
        assertThrows(IllegalArgumentException.class, () ->
                EmailValidator.validateToEmailAddresses("user1@testdomain.com;user2@testdomain.com"));
    }
}
