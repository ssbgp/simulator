package io

/**
 * Created on 16-11-2017
 *
 * @author David Fialho
 *
 * Thrown to indicate that a string label is not valid in that context.
 */
class InvalidLabelException(message: String): Exception(message)