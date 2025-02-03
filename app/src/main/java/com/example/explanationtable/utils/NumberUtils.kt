package com.example.explanationtable.utils

/**
 * A constant array mapping Western digits ('0'..'9') to their Persian numeral equivalents.
 * The index of each element corresponds to the digit's numeric value.
 */
private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

/**
 * Extension function for Int that converts the integer to a String with Persian digits.
 *
 * This function first converts the integer to its standard string representation.
 * Then it iterates over each character and, if the character is a digit, replaces it
 * with the corresponding Persian numeral using the PERSIAN_DIGITS constant.
 * Non-digit characters (such as the negative sign) are preserved.
 *
 * @return A string representing the integer with Persian digits.
 */
fun Int.toPersianDigits(): String = buildString {
    // Convert the integer to a string and iterate over each character.
    for (char in this@toPersianDigits.toString()) {
        if (char.isDigit()) {
            // Convert the digit character to its integer value (e.g., '5' -> 5)
            // and append the corresponding Persian digit.
            append(PERSIAN_DIGITS[char - '0'])
        } else {
            // Append the character unchanged (useful for non-digit characters).
            append(char)
        }
    }
}
