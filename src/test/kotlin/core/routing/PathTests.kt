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

        on("appending a new node") {

            val appendedPath = emptyPath.append(node(1))

            it("returns a path of size 1") {
                assertThat(appendedPath.size, equalTo(1))
            }

            it("keeps the original path empty") {
                assertThat(emptyPath.size, equalTo(0))
            }

        }

    }

    given("a path with 1 node") {

        val path = pathOf(node(1))

        it("has size 1") {
            assertThat(path.size, equalTo(1))
        }

        on("appending a new node") {

            val appendedPath = path.append(node(2))

            it("returns a path of size 2") {
                assertThat(appendedPath.size, equalTo(2))
            }

            it("keeps the original path with size 1") {
                assertThat(path.size, equalTo(1))
            }

        }

    }

})