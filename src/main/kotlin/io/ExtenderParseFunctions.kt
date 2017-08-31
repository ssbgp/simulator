package io

import bgp.BGPRoute
import bgp.policies.interdomain.*
import core.routing.Extender

/**
 * Created on 31-08-2017
 *
 * @author David Fialho
 *
 * This file contains functions to parse extenders from labels.
 */

/**
 * Parses an Interdomain Extender. The supported labels are:
 *
 *  R+ - parsed as a PeerplusExtender
 *  C  - parsed as a CustomerExtender
 *  R  - parsed as a PeerExtender
 *  P  - parsed as a ProviderExtender
 *  S  - parsed as a SiblingExtender
 *
 * This function is NOT case sensitive!
 *
 * @param label      the label of the extender
 * @param lineNumber the number of the line in which the label was found (used for the parse exception message only)
 * @return the extender parsed from the label
 * @throws ParseException if the label is not recognized
 */
@Throws(ParseException::class)
fun parseInterdomainExtender(label: String, lineNumber: Int): Extender<BGPRoute> {

    return when (label.toLowerCase()) {
        "r+" -> PeerplusExtender
        "c" -> CustomerExtender
        "r" -> PeerExtender
        "p" -> ProviderExtender
        "s" -> SiblingExtender
        else -> throw ParseException("Extender label `$label` was not recognized: " +
                "must be either R+, C, R, P, or S", lineNumber)
    }
}

fun parseInterdomainExtender(label: String): Extender<BGPRoute> {
    return parseInterdomainExtender(label, lineNumber = 0)
}