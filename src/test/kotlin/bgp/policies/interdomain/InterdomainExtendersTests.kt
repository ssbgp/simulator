package bgp.policies.interdomain

import bgp.BGPNode
import bgp.BGPRoute
import core.routing.emptyPath
import core.routing.pathOf
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.hamcrest.Matchers.*
import org.hamcrest.MatcherAssert.assertThat

/**
 * Created on 26-07-2017

 * @author David Fialho
 */
object InterdomainExtendersTests : Spek({

    given("a customer extender") {

        on("extending a customer route with an empty AS-PATH") {

            val route = customerRoute(asPath = emptyPath())
            val sender = BGPNode.with(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns a customer route with AS-PATH containing the sender") {
                assertThat(extendedRoute, `is`(customerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a customer route with an AS-PATH containing node 2") {

            val route = customerRoute(asPath = pathOf(BGPNode.with(id = 2)))
            val sender = BGPNode.with(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns a customer route with AS-PATH containing node 2 and the sender") {
                assertThat(extendedRoute, `is`(customerRoute(asPath = pathOf(BGPNode.with(2), sender))))
            }
        }

        on("extending a peer+ route") {

            val route = peerplusRoute(asPath = emptyPath())
            val sender = BGPNode.with(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns a customer route") {
                assertThat(extendedRoute, `is`(customerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a peer route") {

            val route = peerRoute(asPath = emptyPath())
            val sender = BGPNode.with(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns a customer route") {
                assertThat(extendedRoute, `is`(customerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a provider route") {

            val route = providerRoute(asPath = emptyPath())
            val sender = BGPNode.with(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns a customer route") {
                assertThat(extendedRoute, `is`(customerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a self route") {

            val route = BGPRoute.self()
            val sender = BGPNode.with(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns a customer route with the AS-PATH containing the sender") {
                assertThat(extendedRoute, `is`(customerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a invalid route") {

            val route = BGPRoute.invalid()
            val sender = BGPNode.with(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute, `is`(BGPRoute.invalid()))
            }
        }
    }

    given("a peer extender") {

        on("extending a customer route with an empty AS-PATH") {

            val route = customerRoute(asPath = emptyPath())
            val sender = BGPNode.with(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns a peer route with AS-PATH containing the sender") {
                assertThat(extendedRoute, `is`(peerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a customer route with an AS-PATH containing node 2") {

            val route = customerRoute(asPath = pathOf(BGPNode.with(id = 2)))
            val sender = BGPNode.with(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns a peer route with AS-PATH containing node 2 and the sender") {
                assertThat(extendedRoute, `is`(peerRoute(asPath = pathOf(BGPNode.with(2), sender))))
            }
        }

        on("extending a peer+ route") {

            val route = peerplusRoute(asPath = emptyPath())
            val sender = BGPNode.with(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns a peer route") {
                assertThat(extendedRoute, `is`(peerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a peer route") {

            val route = peerRoute(asPath = emptyPath())
            val sender = BGPNode.with(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute, `is`(BGPRoute.invalid()))
            }
        }

        on("extending a provider route") {

            val route = providerRoute(asPath = emptyPath())
            val sender = BGPNode.with(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute, `is`(BGPRoute.invalid()))
            }
        }

        on("extending a self route") {

            val route = BGPRoute.self()
            val sender = BGPNode.with(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns a peer route with the AS-PATH containing the sender") {
                assertThat(extendedRoute, `is`(peerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a invalid route") {

            val route = BGPRoute.invalid()
            val sender = BGPNode.with(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute, `is`(BGPRoute.invalid()))
            }
        }
    }

})
