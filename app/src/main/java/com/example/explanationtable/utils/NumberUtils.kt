// File: app/src/main/java/com/example/explanationtable/utils/NumberUtils.kt
package com.example.explanationtable.utils

/**
 * Extension function to convert an integer to a string with Persian digits.
 */
fun Int.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')
    return this.toString().map { char ->
        if (char.isDigit()) persianDigits[char - '0'] else char
    }.joinToString("")
}
