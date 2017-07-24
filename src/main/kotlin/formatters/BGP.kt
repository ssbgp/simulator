package formatters

import bgp.BGPNode
import bgp.BGPRoute
import com.inamik.text.tables.Cell
import com.inamik.text.tables.GridTable
import com.inamik.text.tables.grid.Border
import core.routing.Path
import core.routing.RoutingTable

/**
 * Created on 24-07-2017.
 *
 * @author David Fialho
 *
 * This file contain extension methods to format components of the BGP protocol, like BGPNodes, BGPRoutes,
 * BGPRoutingTable, etc...
 *
 * The extension methods are all called 'format' and they return a string.
 */
fun BGPNode.format() = this.id.toString()
fun Path<BGPNode>.format() = "[" + map { it.format() }.joinToString() + "]"

fun BGPRoute.format(): String {

    return if (BGPRoute.self() == this || BGPRoute.invalid() == this) {
        this.toString()
    } else {
        "LOCAL-PREF=$localPref, AS-PATH=${asPath.format()}"
    }
}

fun RoutingTable<BGPNode, BGPRoute>.format(): String {

    var textTable = GridTable.of(this.size + 1, 3)
            .put(0, 0, Cell.of("Neighbor"))
            .put(0, 1, Cell.of("Enabled"))
            .put(0, 2, Cell.of("Route"))

    var row = 1
    this.forEach { neighbor, route, enabled ->
        textTable.put(row, 0, Cell.of(neighbor.format()))
        textTable.put(row, 1, Cell.of(enabled.toString()))
        textTable.put(row, 2, Cell.of(route.format()))
        row++
    }

    textTable = Border.SINGLE_LINE.apply(textTable);
    textTable.apply(Cell.Functions.HORIZONTAL_CENTER)

    val builder = StringBuilder()
    for (line in textTable.toCell()) {
        builder.appendln(line)
    }

    return builder.toString()
}