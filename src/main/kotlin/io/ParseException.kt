package io

/**
 * Created on 27-07-2017
 *
 * @author David Fialho
 *
 * Generic parse exception thrown when a parse error occurs while parsing a formatted file.
 */
open class ParseException(message: String, val lineNumber: Int = 0) : Exception(message) {

    override val message: String?
        get() = "${super.message} (in line $lineNumber)"

}
