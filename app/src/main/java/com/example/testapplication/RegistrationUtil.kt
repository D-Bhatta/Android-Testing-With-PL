// RegistrationUtil.kt
package com.example.testapplication

object RegistrationUtil {

    private val users: List<String> = listOf(
        "Tom",
        "Phillip",
        "Sam",
        "Alisha",
        "Erin",
        "Stacey2358",
    )

    /* *
     * the input is not valid if ...
     * ... the username or password is empty
     * ... the password and confirmedPassword do not match
     * ... the username is already taken
     * ... the password length is at least 12 characters
     * ... the password contains at least 2 digits
     */
    fun validateRegistrationInput(
        username: String,
        password: String,
        confirmedPassword: String
    ): Boolean {
        if (username.isEmpty() or password.isEmpty()) {
            return false
        }
        if (!(password.equals(confirmedPassword, ignoreCase = false))) {
            return false
        }
        if (users.contains(username)) {
            return false
        }
        if (password.length < 12) {
            return false
        }
        if (numberOfDigits(password) < 2) {
            return false
        }
        return true
    }

    fun numberOfDigits(string: String): Int {
        var count: Int = 0
        for (letter in string) {
            if (letter.isDigit()) {
                count += 1
            }
        }
        return count
    }
}