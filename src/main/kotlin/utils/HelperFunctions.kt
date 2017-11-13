package utils

/**
 * Created on 31-08-2017
 *
 * @author David Fialho
 *
 * This file contains a set of helper functions and extension function.
 */

/**
 * Parses a string as a non-negative integer number and returns the result
 *
 * @return the non-negative integer
 * @throws NumberFormatException - if the string is not a valid representation of a number.
 */
@Throws(NumberFormatException::class)
fun String.toNonNegativeInt(): Int {

    val value = this.toInt()
    if (value < 0) {
        throw NumberFormatException("For input string \"$this\"")
    }

    return value
}
