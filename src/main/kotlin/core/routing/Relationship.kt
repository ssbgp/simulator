package core.routing

/**
 * Created on 20-07-2017
 *
 * @author David Fialho
 */
data class Relationship<out N: Node, R: Route>(val node: N, val extender: Extender<R>)