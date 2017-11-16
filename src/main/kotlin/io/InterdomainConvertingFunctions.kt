package io

import bgp.BGPRoute
import bgp.policies.interdomain.*
import core.routing.Extender

/**
 * Created on 16-11-2017
 *
 * @author David Fialho
 *
 * This file contains a set of functions and extension methods to convert labels into extenders, or costs, or routes,
 * a functions and extension methods to convert in the other direction: from extenders, costs, or routes, to labels.
 */

//
// Convert from string labels to classes
//

/**
 * Parses String as an InterdomainExtender and returns the result.
 * This method is case sensitive.
 *
 * Valid string labels and corresponding extenders:
 *  R+ -> PeerplusExtender
 *  R* -> PeerstarExtender
 *  C  -> CustomerExtender
 *  R  -> PeerExtender
 *  P  -> ProviderExtender
 *  S  -> SiblingExtender
 *
 * @return the extender corresponding to the string
 * @throws InvalidLabelException if the string does not match any valid extension label
 */
@Throws(InvalidLabelException::class)
fun String.toInterdomainExtender(): Extender<BGPRoute> = when (this) {

    "R+" -> PeerplusExtender
    "R*" -> PeerstarExtender
    "C" -> CustomerExtender
    "R" -> PeerExtender
    "P" -> ProviderExtender
    "S" -> SiblingExtender
    else -> throw InvalidLabelException("extender label '$this' was not recognized, " +
            "it but must be one of R+, R*, C, R, P, and S")
}

/**
 * Parses String as an interdomain local preference value and returns the result.
 * This method is case sensitive.
 *
 * Valid string labels and corresponding local preference values:
 *  r+ -> peer+ route local preference
 *  r* -> peer* route local preference
 *  c  -> customer route local preference
 *  r  -> peer route local preference
 *  p  -> provider route local preference
 *
 * @return the local preference corresponding to the cost label
 * @throws InvalidLabelException if the string does not match any valid cost label
 */
@Throws(InvalidLabelException::class)
fun String.toInterdomainLocalPreference(): Int = when (this) {

    "r+" -> peerplusLocalPreference
    "r*" -> peerstarLocalPreference
    "c" -> customerLocalPreference
    "r" -> peerLocalPreference
    "p" -> providerLocalPreference
    else -> throw InvalidLabelException("cost label '$this' was not recognized, " +
            "it but must be one of r+, r*, c, r, and p")
}

/**
 * Tries to convert [label] to an Interdomain extender. If that fails, then it throws a ParseException.
 * @see #toInterdomainExtender() for more details about how the label is parsed.
 *
 * @param label      the label to parse
 * @param lineNumber the number of the line in which the label was found (used for the parse exception message only)
 * @return the extender corresponding to [label]
 * @throws ParseException if the label is not recognized
 */
@Throws(ParseException::class)
fun parseInterdomainExtender(label: String, lineNumber: Int = 0): Extender<BGPRoute> {

    return try {
        label.toInterdomainExtender()
    } catch (e: InvalidLabelException) {
        throw ParseException(e.message ?: "", lineNumber)
    }
}


// This function is required because it is passed as parameter, which requires a function with this specific signature
@Suppress("NOTHING_TO_INLINE")
@Throws(ParseException::class)
inline fun parseInterdomainExtender(label: String): Extender<BGPRoute> = parseInterdomainExtender(label, lineNumber = 0)


/**
 * Tries to convert [label] to an Interdomain local preference value. If that fails, then it throws a ParseException.
 * @see #toInterdomainLocalPreference() for more details about how the label is parsed.
 *
 * @param label      the label to parse
 * @param lineNumber the number of the line in which the label was found (used for the parse exception message only)
 * @return the local preference value corresponding to [label]
 * @throws ParseException if the label is not recognized
 */
@Suppress("NOTHING_TO_INLINE")
@Throws(ParseException::class)
inline fun parseInterdomainCost(label: String, lineNumber: Int): Int {

    return try {
        label.toInterdomainLocalPreference()
    } catch (e: InvalidLabelException) {
        throw ParseException(e.message ?: "", lineNumber)
    }
}

//
// Convert from classes to string objects
//

fun Int.toInterdomainLabel(): String = when (this) {
    peerplusLocalPreference -> "r+"
    peerstarLocalPreference -> "r*"
    customerLocalPreference -> "c"
    peerLocalPreference -> "r"
    providerLocalPreference -> "p"
    BGPRoute.invalid().localPref -> BGPRoute.invalid().toString()
    BGPRoute.self().localPref -> BGPRoute.self().toString()
    else -> this.toString()
}