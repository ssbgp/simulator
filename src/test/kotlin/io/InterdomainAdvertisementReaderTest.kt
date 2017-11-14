package io

import bgp.BGPRoute
import bgp.policies.interdomain.*
import core.routing.Route
import core.simulator.Advertisement
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasKey
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.Assertions.assertThrows
import testing.`when`
import testing.bgp.BGPNode
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

                then("the advertiser has ID '${advertisement.advertiser.id}'") {
                    assertThat(advertisements, hasKey(advertisement.advertiser.id))
                }

                val (defaultRoute, advertisingTime) = advertisements[advertisement.advertiser.id]!!

                then("the default route is '${advertisement.route}'") {
                    assertThat(defaultRoute, Is(advertisement.route))
                }

                then("the advertising time is '${advertisement.time}'") {
                    assertThat(advertisingTime, Is(advertisement.time))
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

        var exception: ParseException? = null

        it("throws a ParseException") {
            InterdomainAdvertisementReader(StringReader(fileContent)).use {
                exception = assertThrows(ParseException::class.java) {
                    it.read()
                }
            }
        }

        it("indicates the error is in line 2") {
            assertThat(exception?.lineNumber, Is(2))
        }
    }

    context("file with entries `10 = 0 | c`, `15 = 1 | r`, and `5 = 2 | p`") {

        val fileContent = lines(
                "10 = 0 | c",
                "15 = 1 | r",
                "5 = 2 | p"
        )

        `when`("finding advertisement for advertiser 10") {

            val advertisements = InterdomainAdvertisementReader(StringReader(fileContent)).use {
                it.find(listOf(BGPNode(10)))
            }

            it("returns 1 advertisement") {
                assertThat(advertisements.size, Is(1))
            }

            val advertisement = advertisements[0]

            it("has advertiser with ID '10'") {
                assertThat(advertisement.advertiser.id, Is(10))
            }

            it("has customer route") {
                assertThat(advertisement.route, Is(customerRoute()))
            }

            it("has advertising time 0") {
                assertThat(advertisement.time, Is(0))
            }
        }

        `when`("finding advertisement for advertiser 15") {

            val advertisements = InterdomainAdvertisementReader(StringReader(fileContent)).use {
                it.find(listOf(BGPNode(15)))
            }

            it("returns 1 advertisement") {
                assertThat(advertisements.size, Is(1))
            }

            val advertisement = advertisements[0]

            it("has advertiser with ID '15'") {
                assertThat(advertisement.advertiser.id, Is(15))
            }

            it("has peer route") {
                assertThat(advertisement.route, Is(peerRoute()))
            }

            it("has advertising time 1") {
                assertThat(advertisement.time, Is(1))
            }
        }

        `when`("finding advertisement for advertisers 5 and 15") {

            val advertisements = InterdomainAdvertisementReader(StringReader(fileContent)).use {
                it.find(listOf(BGPNode(5), BGPNode(15)))
            }

            it("returns 2 advertisements") {
                assertThat(advertisements.size, Is(2))
            }
        }

    }

})