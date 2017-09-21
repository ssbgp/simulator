package bgp.policies.interdomain

import bgp.bgpRouteCompare
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.hamcrest.Matchers.`is` as Is


/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
object InterdomainCompareRoutesTests: Spek({

    context("AS-PATHs are always empty") {

        on("comparing two customer routes") {

            it("returns they have the same preference") {
                assertThat(bgpRouteCompare(customerRoute(), customerRoute()),
                        Is(equalTo(0)))
            }
        }

        on("comparing a customer route and a peer+ route") {

            it("returns peer+ route has higher preference") {
                assertThat(bgpRouteCompare(peerplusRoute(), customerRoute()),
                        Is(greaterThan(0)))
            }
        }

        on("comparing a customer route and a peer route") {

            it("returns customer route has higher preference") {
                assertThat(bgpRouteCompare(customerRoute(), peerRoute()),
                        Is(greaterThan(0)))
            }
        }

        on("comparing a customer route and a provider route") {

            it("returns customer route has higher preference") {
                assertThat(bgpRouteCompare(customerRoute(), providerRoute()),
                        Is(greaterThan(0)))
            }
        }

        on("comparing a customer route and a peer* route") {

            it("returns peer* route has higher preference") {
                assertThat(bgpRouteCompare(peerstarRoute(), customerRoute()),
                        Is(greaterThan(0)))
            }
        }

        on("comparing a peer+ route and a peer* route") {

            it("returns peer+ route has higher preference") {
                assertThat(bgpRouteCompare(peerplusRoute(), peerstarRoute()),
                        Is(greaterThan(0)))
            }
        }

        on("comparing a customer route with 0 sibling hops with customer route with 1 sibling hop") {

            it("returns route with 0 sibling hops has higher preference") {
                assertThat(bgpRouteCompare(customerRoute(siblingHops = 0), customerRoute(siblingHops = 1)),
                        Is(greaterThan(0)))
            }
        }

        on("comparing a customer route with 0 sibling hops with peer+ route with 10 sibling hops") {

            it("returns peer+ route has higher preference") {
                assertThat(bgpRouteCompare(peerplusRoute(siblingHops = 10), customerRoute(siblingHops = 0)),
                        Is(greaterThan(0)))
            }
        }

        on("comparing a customer route with 10 sibling hops with peer route with 0 sibling hops") {

            it("returns peer+ route has higher preference") {
                assertThat(bgpRouteCompare(customerRoute(siblingHops = 10), peerRoute(siblingHops = 0)),
                        Is(greaterThan(0)))
            }
        }

        on("comparing a peer+ route with 0 sibling hops with peer+ route with 1 sibling hop") {

            it("returns route with 0 sibling hops has higher preference") {
                assertThat(bgpRouteCompare(peerplusRoute(siblingHops = 0), peerplusRoute(siblingHops = 1)),
                        Is(greaterThan(0)))
            }
        }

        on("comparing a peer route with 0 sibling hops with peer route with 1 sibling hop") {

            it("returns route with 0 sibling hops has higher preference") {
                assertThat(bgpRouteCompare(peerRoute(siblingHops = 0), peerRoute(siblingHops = 1)),
                        Is(greaterThan(0)))
            }
        }

        on("comparing a provider route with 0 sibling hops with provider route with 1 sibling hop") {

            it("returns route with 0 sibling hops has higher preference") {
                assertThat(bgpRouteCompare(providerRoute(siblingHops = 0), providerRoute(siblingHops = 1)),
                        Is(greaterThan(0)))
            }
        }
    }

})