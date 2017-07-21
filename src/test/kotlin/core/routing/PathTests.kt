package core.routing

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import testing.node

/**
 * Created on 21-07-2017

 * @author David Fialho
 */
object PathTests : Spek({

    given("an empty path") {

        val emptyPath = emptyPath<Node>()

        it("has size 0") {
            assertThat(emptyPath.size, equalTo(0))
        }

    }

    given("a path with 1 node") {
        val path = pathOf(node(1))

        it("has size 1") {
            assertThat(path.size, equalTo(1))
        }

    }

})