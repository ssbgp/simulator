package bgp.policies.interdomain

import bgp.BGPRoute
import core.routing.emptyPath
import core.routing.pathOf
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.bgp.BGPNode
import org.hamcrest.Matchers.`is` as Is

/**
 * Created on 26-07-2017

 * @author David Fialho
 */
object InterdomainExtendersTests : Spek({

    given("a customer extender") {

        on("extending a customer route with an empty AS-PATH") {

            val route = customerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns a customer route with AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(customerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a customer route with an AS-PATH containing node 2") {

            val route = customerRoute(asPath = pathOf(BGPNode(id = 2)))
            val sender = BGPNode(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns a customer route with AS-PATH containing node 2 and the sender") {
                assertThat(extendedRoute,
                        Is(customerRoute(asPath = pathOf(BGPNode(2), sender))))
            }
        }

        on("extending a peer+ route") {

            val route = peerplusRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns a customer route") {
                assertThat(extendedRoute,
                        Is(customerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a peer* route") {

            val route = peerstarRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a peer route") {

            val route = peerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a provider route") {

            val route = providerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a self route") {

            val route = BGPRoute.self()
            val sender = BGPNode(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns a customer route with the AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(customerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a invalid route") {

            val route = BGPRoute.invalid()
            val sender = BGPNode(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a customer route with 1 sibling hop") {

            val route = customerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns a customer route with 0 sibling hops") {
                assertThat(extendedRoute,
                        Is(customerRoute(siblingHops = 0, asPath = pathOf(sender))))
            }
        }

        on("extending a peer+ route with 1 sibling hop") {

            val route = peerplusRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns a customer route with 0 sibling hops") {
                assertThat(extendedRoute,
                        Is(customerRoute(siblingHops = 0, asPath = pathOf(sender))))
            }
        }

        on("extending a peer route with 1 sibling hop") {

            val route = peerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a provider route with 1 sibling hop") {

            val route = providerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = CustomerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }
    }

    given("a peer extender") {

        on("extending a customer route with an empty AS-PATH") {

            val route = customerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns a peer route with AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(peerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a customer route with an AS-PATH containing node 2") {

            val route = customerRoute(asPath = pathOf(BGPNode(id = 2)))
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns a peer route with AS-PATH containing node 2 and the sender") {
                assertThat(extendedRoute,
                        Is(peerRoute(asPath = pathOf(BGPNode(2), sender))))
            }
        }

        on("extending a peer+ route") {

            val route = peerplusRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns a peer route") {
                assertThat(extendedRoute,
                        Is(peerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a peer* route") {

            val route = peerstarRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a peer route") {

            val route = peerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a provider route") {

            val route = providerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a self route") {

            val route = BGPRoute.self()
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns a peer route with the AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(peerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a invalid route") {

            val route = BGPRoute.invalid()
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a customer route with 1 sibling hop") {

            val route = customerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns a peer route with 0 sibling hops") {
                assertThat(extendedRoute,
                        Is(peerRoute(siblingHops = 0, asPath = pathOf(sender))))
            }
        }

        on("extending a peer+ route with 1 sibling hop") {

            val route = peerplusRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns a peer route with 0 sibling hops") {
                assertThat(extendedRoute,
                        Is(peerRoute(siblingHops = 0, asPath = pathOf(sender))))
            }
        }

        on("extending a peer route with 1 sibling hop") {

            val route = peerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a provider route with 1 sibling hop") {

            val route = providerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }
    }

    given("a provider extender") {

        on("extending a customer route with an empty AS-PATH") {

            val route = customerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = ProviderExtender.extend(route, sender)

            it("returns a provider route with AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(providerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a customer route with an AS-PATH containing node 2") {

            val route = customerRoute(asPath = pathOf(BGPNode(id = 2)))
            val sender = BGPNode(id = 1)

            val extendedRoute = ProviderExtender.extend(route, sender)

            it("returns a provider route with AS-PATH containing node 2 and the sender") {
                assertThat(extendedRoute,
                        Is(providerRoute(asPath = pathOf(BGPNode(2), sender))))
            }
        }

        on("extending a peer+ route") {

            val route = peerplusRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = ProviderExtender.extend(route, sender)

            it("returns a provider route") {
                assertThat(extendedRoute,
                        Is(providerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a peer* route") {

            val route = peerstarRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = ProviderExtender.extend(route, sender)

            it("returns provider route") {
                assertThat(extendedRoute,
                        Is(providerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a peer route") {

            val route = peerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = ProviderExtender.extend(route, sender)

            it("returns provider route") {
                assertThat(extendedRoute,
                        Is(providerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a provider route") {

            val route = providerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = ProviderExtender.extend(route, sender)

            it("returns provider route") {
                assertThat(extendedRoute,
                        Is(providerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a self route") {

            val route = BGPRoute.self()
            val sender = BGPNode(id = 1)

            val extendedRoute = ProviderExtender.extend(route, sender)

            it("returns a provider route with the AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(providerRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a invalid route") {

            val route = BGPRoute.invalid()
            val sender = BGPNode(id = 1)

            val extendedRoute = ProviderExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a customer route with 1 sibling hop") {

            val route = customerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = ProviderExtender.extend(route, sender)

            it("returns a provider route with 0 sibling hops") {
                assertThat(extendedRoute,
                        Is(providerRoute(siblingHops = 0, asPath = pathOf(sender))))
            }
        }

        on("extending a peer+ route with 1 sibling hop") {

            val route = peerplusRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = ProviderExtender.extend(route, sender)

            it("returns a provider route with 0 sibling hops") {
                assertThat(extendedRoute,
                        Is(providerRoute(siblingHops = 0, asPath = pathOf(sender))))
            }
        }

        on("extending a peer route with 1 sibling hop") {

            val route = peerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = ProviderExtender.extend(route, sender)

            it("returns a provider route with 0 sibling hops") {
                assertThat(extendedRoute,
                        Is(providerRoute(siblingHops = 0, asPath = pathOf(sender))))
            }
        }

        on("extending a provider route with 1 sibling hop") {

            val route = providerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = ProviderExtender.extend(route, sender)

            it("returns a provider route with 0 sibling hops") {
                assertThat(extendedRoute,
                        Is(providerRoute(siblingHops = 0, asPath = pathOf(sender))))
            }
        }
    }

    given("a peer+ extender") {

        on("extending a customer route with an empty AS-PATH") {

            val route = customerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerplusExtender.extend(route, sender)

            it("returns a peer+ route with AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(peerplusRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a customer route with an AS-PATH containing node 2") {

            val route = customerRoute(asPath = pathOf(BGPNode(id = 2)))
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerplusExtender.extend(route, sender)

            it("returns a peer+ route with AS-PATH containing node 2 and the sender") {
                assertThat(extendedRoute,
                        Is(peerplusRoute(asPath = pathOf(BGPNode(2), sender))))
            }
        }

        on("extending a peer+ route") {

            val route = peerplusRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerplusExtender.extend(route, sender)

            it("returns a peer+ route") {
                assertThat(extendedRoute,
                        Is(peerplusRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a peer* route") {

            val route = peerstarRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerplusExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a peer route") {

            val route = peerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerplusExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a provider route") {

            val route = providerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerplusExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a self route") {

            val route = BGPRoute.self()
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerplusExtender.extend(route, sender)

            it("returns a peer+ route with the AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(peerplusRoute(asPath = pathOf(sender))))
            }
        }

        on("extending an invalid route") {

            val route = BGPRoute.invalid()
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerplusExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a customer route with 1 sibling hop") {

            val route = customerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerplusExtender.extend(route, sender)

            it("returns a peer+ route with 0 sibling hops") {
                assertThat(extendedRoute,
                        Is(peerplusRoute(siblingHops = 0, asPath = pathOf(sender))))
            }
        }

        on("extending a peer+ route with 1 sibling hop") {

            val route = peerplusRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerplusExtender.extend(route, sender)

            it("returns a peer+ route with 0 sibling hops") {
                assertThat(extendedRoute,
                        Is(peerplusRoute(siblingHops = 0, asPath = pathOf(sender))))
            }
        }

        on("extending a peer route with 1 sibling hop") {

            val route = peerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerplusExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a provider route with 1 sibling hop") {

            val route = providerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = PeerplusExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }
    }

    given("a peer* extender") {

        val extender = PeerstarExtender

        on("extending a customer route with an empty AS-PATH") {

            val route = customerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = extender.extend(route, sender)

            it("returns a peer* route with AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(peerstarRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a customer route with an AS-PATH containing node 2") {

            val route = customerRoute(asPath = pathOf(BGPNode(id = 2)))
            val sender = BGPNode(id = 1)

            val extendedRoute = extender.extend(route, sender)

            it("returns a peer* route with AS-PATH containing node 2 and the sender") {
                assertThat(extendedRoute,
                        Is(peerstarRoute(asPath = pathOf(BGPNode(2), sender))))
            }
        }

        on("extending a peer+ route") {

            val route = peerplusRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = extender.extend(route, sender)

            it("returns a peer* route") {
                assertThat(extendedRoute,
                        Is(peerstarRoute(asPath = pathOf(sender))))
            }
        }

        on("extending a peer* route") {

            val route = peerstarRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = extender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a peer route") {

            val route = peerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = extender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a provider route") {

            val route = providerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = extender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a self route") {

            val route = BGPRoute.self()
            val sender = BGPNode(id = 1)

            val extendedRoute = extender.extend(route, sender)

            it("returns a peer* route with the AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(peerstarRoute(asPath = pathOf(sender))))
            }
        }

        on("extending an invalid route") {

            val route = BGPRoute.invalid()
            val sender = BGPNode(id = 1)

            val extendedRoute = extender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a customer route with 1 sibling hop") {

            val route = customerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = extender.extend(route, sender)

            it("returns a peer* route with 0 sibling hops") {
                assertThat(extendedRoute,
                        Is(peerstarRoute(siblingHops = 0, asPath = pathOf(sender))))
            }
        }

        on("extending a peer+ route with 1 sibling hop") {

            val route = peerplusRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = extender.extend(route, sender)

            it("returns a peer* route with 0 sibling hops") {
                assertThat(extendedRoute,
                        Is(peerstarRoute(siblingHops = 0, asPath = pathOf(sender))))
            }
        }

        on("extending a peer route with 1 sibling hop") {

            val route = peerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = extender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a provider route with 1 sibling hop") {

            val route = providerRoute(siblingHops = 1, asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = extender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }
    }

    given("a sibling extender") {

        on("extending a customer route with 0 sibling hops and with an empty AS-PATH") {

            val route = customerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = SiblingExtender.extend(route, sender)

            it("returns a customer route with 1 sibling hop and with AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(customerRoute(siblingHops = 1, asPath = pathOf(sender))))
            }
        }

        on("extending a customer route with 0 sibling hops and  with an AS-PATH containing node 2") {

            val route = customerRoute(asPath = pathOf(BGPNode(id = 2)))
            val sender = BGPNode(id = 1)

            val extendedRoute = SiblingExtender.extend(route, sender)

            it("returns a customer route with 1 sibling hop and with AS-PATH containing node 2 and the sender") {
                assertThat(extendedRoute,
                        Is(customerRoute(siblingHops = 1, asPath = pathOf(BGPNode(2), sender))))
            }
        }

        on("extending a self route") {

            val route = BGPRoute.self()
            val sender = BGPNode(id = 1)

            val extendedRoute = SiblingExtender.extend(route, sender)

            it("returns a customer route with 1 sibling hop and with AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(customerRoute(siblingHops = 1, asPath = pathOf(sender))))
            }
        }

        on("extending an invalid route") {

            val route = BGPRoute.invalid()
            val sender = BGPNode(id = 1)

            val extendedRoute = SiblingExtender.extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute,
                        Is(BGPRoute.invalid()))
            }
        }

        on("extending a peer+ route") {

            val route = peerplusRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = SiblingExtender.extend(route, sender)

            it("returns a peer+ route with 1 sibling hop and with AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(peerplusRoute(siblingHops = 1, asPath = pathOf(sender))))
            }
        }

        on("extending a peer* route") {

            val route = peerstarRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = SiblingExtender.extend(route, sender)

            it("returns a peer* route with 1 sibling hop and with AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(peerstarRoute(siblingHops = 1, asPath = pathOf(sender))))
            }
        }

        on("extending a peer route") {

            val route = peerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = SiblingExtender.extend(route, sender)

            it("returns a peer route with 1 sibling hop and with AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(peerRoute(siblingHops = 1, asPath = pathOf(sender))))
            }
        }

        on("extending a provider route") {

            val route = providerRoute(asPath = emptyPath())
            val sender = BGPNode(id = 1)

            val extendedRoute = SiblingExtender.extend(route, sender)

            it("returns a provider route with 1 sibling hop and with AS-PATH containing the sender") {
                assertThat(extendedRoute,
                        Is(providerRoute(siblingHops = 1, asPath = pathOf(sender))))
            }
        }
    }

})
