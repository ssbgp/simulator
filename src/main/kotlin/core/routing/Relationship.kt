package core

import core.routing.Extender
import core.routing.Node
import core.routing.Route

/**
 * Created on 20-07-2017
 *
 * @author David Fialho
 */
data class Relationship<out N: Node, R: Route>(val node: N, val extender: Extender<R>)