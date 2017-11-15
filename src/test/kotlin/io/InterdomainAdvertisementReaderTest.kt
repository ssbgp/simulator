package io

import bgp.BGPRoute
import bgp.policies.interdomain.*
import core.routing.Route
import core.simulator.Advertisement
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.Assertions.assertThrows
import testing.node
import testing.then
import java.io.StringReader
import org.hamcrest.Matchers.`is` as Is

object InterdomainAdvertisementReaderTest: Spek({

    infix fun <R: Route> String.isParsedTo(advertisement: Advertisement<R>): Pair<String, Advertisement<R>> =
            Pair(this, advertisement)

    context("an interdomain advertisements file with a single entry") {

        listOf(
                "10 = 0 | c" isParsedTo Advertisement(node(10), customerRoute(), time = 0),
                "11 = 0 | c" isParsedTo Advertisement(node(11), customerRoute(), time = 0),
                "10 = 15 | c" isParsedTo Advertisement(node(10), customerRoute(), time = 15),
                "10 = 15 | r" isParsedTo Advertisement(node(10), peerRoute(), time = 15),
                "10 = 15 | p" isParsedTo Advertisement(node(10), providerRoute(), time = 15),
                "10 = 15 | r+" isParsedTo Advertisement(node(10), peerplusRoute(), time = 15),
                "10 = 15 | r*" isParsedTo Advertisement(node(10), peerstarRoute(), time = 15),
                "10 = | c" isParsedTo Advertisement(node(10), customerRoute(), time = 0),
                "10 = 15 | " isParsedTo Advertisement(node(10), BGPRoute.self(), time = 15),
                "10 = | " isParsedTo Advertisement(node(10), BGPRoute.self(), time = 0),
                "10 = " isParsedTo Advertisement(node(10), BGPRoute.self(), time = 0),
                "10 = 15" isParsedTo Advertisement(node(10), BGPRoute.self(), time = 15)

        ).forEach { (line, advertisement) ->

            given("entry is `$line`") {

                val advertisements = InterdomainAdvertisementReader(StringReader(line)).use {
                    it.read()
                }

                then("it reads 1 advertisement") {
                    assertThat(advertisements.size, Is(1))
                }

                val info = advertisements[0]

                then("the advertiser has ID '${advertisement.advertiser.id}'") {
                    assertThat(info.advertiserID, Is(advertisement.advertiser.id))
                }

                then("the default route is '${advertisement.route}'") {
                    assertThat(info.defaultRoute, Is(advertisement.route))
                }

                then("the advertising time is '${advertisement.time}'") {
                    assertThat(info.time, Is(advertisement.time))
                }
            }
        }

        listOf(
                "a = 0 | c",
                "10 = 0 | c | 10",
                "10 = 0 | b ",  // 'b' is not a valid interdomain cost label
                "10 = -1 | c ", // advertise time must be non-negative
                "10 = a | c ",
                "10 = 0 | c | "

        ).forEach { line ->

            given("invalid entry is `$line`") {

                var exception: ParseException? = null

                it("throws a ParseException") {
                    InterdomainAdvertisementReader(StringReader(line)).use {
                        exception = assertThrows(ParseException::class.java) {
                            it.read()
                        }
                    }
                }

                it("indicates the error is in line 1") {
                    assertThat(exception?.lineNumber, Is(1))
                }
            }
        }
    }


    given("file with entries `10 = 0 | c` and `10 = 1 | r`") {

        val fileContent = lines(
                "10 = 0 | c",
                "10 = 1 | r"
        )

        val advertisements = InterdomainAdvertisementReader(StringReader(fileContent)).use {
            it.read()
        }

        it("reads 2 advertisements") {
            assertThat(advertisements.size, Is(2))
        }

        it("reads one advertisement with advertiser ID 10") {
            assertThat(advertisements[0].advertiserID, Is(10))
        }

        it("reads one advertisement with a customer route") {
            assertThat(advertisements[0].defaultRoute, Is(customerRoute()))
        }

        it("reads one advertisement with advertising time 0") {
            assertThat(advertisements[0].time, Is(0))
        }

        it("reads another advertisement with advertiser ID 10") {
            assertThat(advertisements[1].advertiserID, Is(10))
        }

        it("reads another advertisement with a peer route") {
            assertThat(advertisements[1].defaultRoute, Is(peerRoute()))
        }

        it("reads another advertisement with advertising time 1") {
            assertThat(advertisements[1].time, Is(1))
        }
    }

})