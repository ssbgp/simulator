package io

import bgp.BGPRoute
import core.routing.NodeID
import core.routing.pathOf
import utils.toNonNegativeInt
import java.io.IOException
import java.io.Reader

/**
 * Created on 14-11-2017
 *
 * @author David Fialho
 */
class InterdomainAdvertisementReader(reader: Reader): AutoCloseable {

    companion object {
        val DEFAULT_ADVERTISING_TIME = 1
        val DEFAULT_DEFAULT_ROUTE = BGPRoute.self()
    }

    private class EntryHandler(val advertisements: MutableMap<NodeID, AdvertisementInfo<BGPRoute>>)
        : KeyValueParser.Handler {

        /**
         * Invoked when a new entry is parsed.
         *
         * @param entry       the parsed entry
         * @param currentLine line number where the node was parsed
         */
        override fun onEntry(entry: KeyValueParser.Entry, currentLine: Int) {

            val advertiserID = try {
                entry.key.toNonNegativeInt()
            } catch (e: NumberFormatException) {
                throw ParseException("advertising node ID must be a non-negative integer value, but was '${entry.key}'")
            }

            // The first value is the advertising time - this value is NOT mandatory
            val timeValue = try { entry.values[0] } catch (e: IndexOutOfBoundsException) { "" }

            val time = if (timeValue.isBlank()) DEFAULT_ADVERTISING_TIME else try {
                 timeValue.toNonNegativeInt()
            } catch (e: NumberFormatException) {
                throw ParseException("advertising time must be a non-negative integer value, but was '$timeValue'")
            }

            // The second value is a cost label for the default route's local preference
            val label = try { entry.values[1] } catch (e: IndexOutOfBoundsException) { "" }
            val defaultRoute = if (label.isBlank())
                DEFAULT_DEFAULT_ROUTE
            else
                BGPRoute.with(parseInterdomainCost(label, currentLine), pathOf())

            advertisements.put(advertiserID, AdvertisementInfo(defaultRoute, time))
        }

    }

    private val parser = KeyValueParser(reader)

    /**
     * Reads a map, mapping advertiser IDs to pairs of default routes and advertising times.
     *
     * @throws ParseException if the format of the input is invalid
     * @throws IOException if an IO error occurs
     * @return a list of advertisements read from the stream
     */
    @Throws(ParseException::class, IOException::class)
    fun read(): Map<NodeID, AdvertisementInfo<BGPRoute>> {
        val advertisements = HashMap<NodeID, AdvertisementInfo<BGPRoute>>()
        parser.parse(EntryHandler(advertisements))
        return advertisements
    }

    /**
     * Closes the underlying stream.
     */
    override fun close() {
        parser.close()
    }
}
