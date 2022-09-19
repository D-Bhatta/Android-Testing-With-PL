package com.example.testapplication

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RegistrationUtilTest {

    @Test
    fun `empty username returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "",
            password = "za5la3Ich5cocuat",
            confirmedPassword = "za5la3Ich5cocuat"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `valid username and correctly repeated password returns true`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "Rachel",
            password = "za5la3Ich5cocuat",
            confirmedPassword = "za5la3Ich5cocuat"
        )
        assertThat(result).isTrue()
    }

    @Test
    fun `username already exists returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "Tom",
            password = "za5la3Ich5cocuat",
            confirmedPassword = "za5la3Ich5cocuat"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `empty password returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "Rachel",
            password = "",
            confirmedPassword = ""
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `password and confirmed password do not match returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "Rachel",
            password = "za5la3Ich5cocuat",
            confirmedPassword = "notmatchingpass"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `password length less than 12 characters returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "Rachel",
            password = "za5la",
            confirmedPassword = "za5la"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `password contains less than 2 digits returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "Rachel",
            password = "side1burns",
            confirmedPassword = "side1burns"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `number of digits in 541132 is 6`() {
        val result = RegistrationUtil.numberOfDigits("541132")
        assertThat(result).isEqualTo(6)
    }

    @Test
    fun `number of digits in 112david is 3`() {
        val result = RegistrationUtil.numberOfDigits("112david")
        assertThat(result).isEqualTo(3)
    }

    @Test
    fun `number of digits in red41riding is 2`() {
        val result = RegistrationUtil.numberOfDigits("red41riding")
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `number of digits in blue67one29pink is 4`() {
        val result = RegistrationUtil.numberOfDigits("blue67one29pink")
        assertThat(result).isEqualTo(4)
    }

    @Test
    fun `number of digits in tint91 is 2`() {
        val result = RegistrationUtil.numberOfDigits("tint91")
        assertThat(result).isEqualTo(2)
    }
}