package core.routing

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.node
import org.hamcrest.Matchers.`is` as Is


/**
 * Created on 21-07-2017

 * @author David Fialho
 */
object PathTests : Spek({

    given("an empty path") {

        val emptyPath by memoized { emptyPath() }

        it("has size 0") {
            assertThat(emptyPath.size,
                    Is(equalTo(0)))
        }

        on("appending a new node") {

            val appendedPath = emptyPath.append(node(1))

            it("returns a path of size 1") {
                assertThat(appendedPath.size,
                        Is(equalTo(1)))
            }

            it("returns a path containing that new node") {
                assertThat(node(1) in appendedPath,
                        Is(true))
            }

            it("keeps the original path empty") {
                assertThat(emptyPath.size,
                        Is(equalTo(0)))
            }
        }

        on("obtaining sub-path before some node") {

            val subPath = emptyPath.subPathBefore(node(1))

            it("returns an empty path") {
                assertThat(subPath,
                        Is(emptyPath()))
            }
        }
    }

    given("a path with one node with ID 1") {

        val path by memoized { pathOf(node(1)) }

        it("has size 1") {
            assertThat(path.size,
                    Is(equalTo(1)))
        }

        on("appending node 2") {

            val appendedPath = path.append(node(2))

            it("returns a path of size 2") {
                assertThat(appendedPath.size,
                        Is(equalTo(2)))
            }

            it("returns path [1, 2]") {
                assertThat(appendedPath,
                        Is(pathOf(node(1), node(2))))
            }

            it("keeps the original path with size 1") {
                assertThat(path.size,
                        Is(equalTo(1)))
            }
        }

        on("obtaining sub-path before node 1") {

            val subPath = path.subPathBefore(node(1))

            it("returns empty path") {
                assertThat(subPath,
                        Is(emptyPath()))
            }
        }

        on("obtaining sub-path before node 2") {

            val subPath = path.subPathBefore(node(2))

            it("returns original path with node 1") {
                assertThat(subPath,
                        Is(path))
            }
        }

    }

    given("a path with nodes 1 and 2") {

        val path = pathOf(node(1), node(2))

        it("contains node 1") {
            assertThat(node(1) in path,
                    Is(true))
        }

        it("contains node 2") {
            assertThat(node(2) in path,
                    Is(true))
        }

        it("does not contain node 3") {
            assertThat(node(3) in path,
                    Is(false))
        }

        on("copying the path") {

            val pathCopy = path.copy()

            it("returns a new path instance") {
                assertThat(pathCopy,
                        Is(not(sameInstance(path))))
            }

            it("returns a path equal to the initial path") {
                assertThat(pathCopy,
                        Is(equalTo(path)))
            }
        }

    }

    context("two paths with the same size and the same nodes in the exact same order") {

        val path1 = pathOf(node(1), node(2))
        val path2 = pathOf(node(1), node(2))

        it("are equal") {
            assertThat(path1,
                    Is(equalTo(path2)))
        }

    }

    context("two paths with different sizes and matching nodes") {

        val path1 = pathOf(node(1), node(2), node(3))
        val path2 = pathOf(node(1), node(2))

        it("are different") {
            assertThat(path1,
                    Is(not(equalTo(path2))))
        }

    }

    context("two paths with the same size but different nodes") {

        val path1 = pathOf(node(1), node(2))
        val path2 = pathOf(node(2), node(3))

        it("are different") {
            assertThat(path1,
                    Is(not(equalTo(path2))))
        }

    }

    context("two paths with the same size and the same nodes in different order") {

        val path1 = pathOf(node(1), node(2))
        val path2 = pathOf(node(2), node(1))

        it("are different") {
            assertThat(path1,
                    Is(not(equalTo(path2))))
        }

    }

    given("a path [1, 2, 3, 4, 5]") {

        val path = pathOf(node(1), node(2), node(3), node(4), node(5))

        on("obtaining sub-path before node 1") {

            val subPath = path.subPathBefore(node(1))

            it("returns empty path") {
                assertThat(subPath,
                        Is(emptyPath()))
            }
        }

        on("obtaining sub-path before node 2") {

            val subPath = path.subPathBefore(node(2))

            it("returns path with a single node 1") {
                assertThat(subPath,
                        Is(pathOf(node(1))))
            }
        }

        on("obtaining sub-path before node 3") {

            val subPath = path.subPathBefore(node(3))

            it("returns path [1, 2]") {
                assertThat(subPath,
                        Is(pathOf(node(1), node(2))))
            }
        }

        on("obtaining sub-path before node 4") {

            val subPath = path.subPathBefore(node(4))

            it("returns path [1, 2, 3]") {
                assertThat(subPath,
                        Is(pathOf(node(1), node(2), node(3))))
            }
        }

        on("obtaining sub-path before node 5") {

            val subPath = path.subPathBefore(node(5))

            it("returns path [1, 2, 3, 4]") {
                assertThat(subPath,
                        Is(pathOf(node(1), node(2), node(3), node(4))))
            }
        }

        on("obtaining sub-path before node 6") {

            val subPath = path.subPathBefore(node(6))

            it("returns path [1, 2, 3, 4, 5]") {
                assertThat(subPath,
                        Is(pathOf(node(1), node(2), node(3), node(4), node(5))))
            }
        }
    }

    given("a path [1, 1]") {

        val path = pathOf(node(1), node(1))

        on("obtaining sub-path before node 1") {

            val subPath = path.subPathBefore(node(1))

            it("returns an empty path") {
                assertThat(subPath,
                        Is(emptyPath()))
            }
        }
    }

    given("a path [2, 1, 3, 1]") {

        val path = pathOf(node(2), node(1), node(3), node(1))

        on("obtaining sub-path before node 1") {

            val subPath = path.subPathBefore(node(1))

            it("returns path with node 2") {
                assertThat(subPath,
                        Is(pathOf(node(2))))
            }
        }
    }

})