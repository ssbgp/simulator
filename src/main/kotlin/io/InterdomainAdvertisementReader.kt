package io

import bgp.BGPRoute
import core.routing.NodeID
import core.routing.pathOf
import core.simulator.Advertisement
import core.simulator.Advertiser
import utils.toNonNegativeInt
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Reader


private val DEFAULT_ADVERTISING_TIME = 0
private val DEFAULT_DEFAULT_ROUTE = BGPRoute.self()


/**
 * Created on 14-11-2017
 *
 * @author David Fialho
 */
class InterdomainAdvertisementReader(reader: Reader): AutoCloseable {

    constructor(file: File): this(FileReader(file))

    private class Handler(
            val advertisements: MutableMap<NodeID, AdvertisementInfo<BGPRoute>>,
            val advertisers: Map<NodeID, Advertiser<BGPRoute>>? = null
    ): KeyValueParser.Handler {

        /**
         * Invoked when a new entry is parsed.
         *
         * @param entry       the parsed entry
         * @param currentLine line number where the node was parsed
         */
        override fun onEntry(entry: KeyValueParser.Entry, currentLine: Int) {

            if (entry.values.size > 2) {
                throw ParseException("only 2 values are expected for an advertiser, " +
                        "but ${entry.values.size} were given", currentLine)
            }

            // The key corresponds to the advertiser ID
            val advertiserID = try {
                entry.key.toNonNegativeInt()
            } catch (e: NumberFormatException) {
                throw ParseException("advertising node ID must be a non-negative integer value, " +
                        "but was '${entry.key}'", currentLine)
            }

            // Ignore entry if accepted advertisers are defined
            if (advertisers != null && advertiserID !in advertisers) {
                return  // ignore this entry
            }

            // The first value is the advertising time - this value is NOT mandatory
            // The KeyValueParser ensure that there is at least one value always, even if it is blank
            val timeValue = entry.values[0]
            val time = if (timeValue.isBlank()) DEFAULT_ADVERTISING_TIME else try {
                timeValue.toNonNegativeInt()
            } catch (e: NumberFormatException) {
                throw ParseException("advertising time must be a non-negative integer value, " +
                        "but was '$timeValue'", currentLine)
            }

            // The second value is a cost label for the default route's local preference - this value is NOT mandatory
            val defaultRoute = if (entry.values.size == 1 || entry.values[1].isBlank()) {
                DEFAULT_DEFAULT_ROUTE
            } else {
                BGPRoute.with(parseInterdomainCost(entry.values[1], currentLine), pathOf())
            }

            if (advertisements.putIfAbsent(advertiserID, AdvertisementInfo(defaultRoute, time)) != null) {
                throw ParseException("advertiser $advertiserID is defined twice", currentLine)
            }
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
        parser.parse(Handler(advertisements))
        return advertisements
    }

    /**
     * Finds the advertisements for each advertiser in [advertisers] and returns the corresponding list of
     * advertisements. An advertisement with the default values is chosen for advertisers without a defined
     * advertisement.
     */
    @Throws(ParseException::class, IOException::class)
    fun find(advertisers: List<Advertiser<BGPRoute>>): List<Advertisement<BGPRoute>> {
        val advertisements = HashMap<NodeID, AdvertisementInfo<BGPRoute>>()
        val advertisersByID = advertisers.associateBy({ it.id }, { it })

        parser.parse(Handler(advertisements, advertisersByID))

        return advertisers.map {
            val info = advertisements[it.id]
            Advertisement(
                    advertiser = it,
                    route = info?.defaultRoute ?: DEFAULT_DEFAULT_ROUTE,
                    time = info?.time ?: DEFAULT_ADVERTISING_TIME
            )
        }
    }

    /**
     * Closes the underlying stream.
     */
    override fun close() {
        parser.close()
    }
}
