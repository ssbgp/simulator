package core.routing2

import testing2.node
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is` as Is
import org.hamcrest.Matchers.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on


/**
 * Created on 21-07-2017

 * @author David Fialho
 */
object PathTests : Spek({

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

    }

    given("an empty path") {

        val emptyPath = emptyPath()

        it("has size 0") {
            assertThat(emptyPath.size, equalTo(0))
        }

        on("appending a new node") {

            val appendedPath = emptyPath.append(node(1))

            it("returns a path of size 1") {
                assertThat(appendedPath.size, equalTo(1))
            }

            it("returns a path containing that new node") {
                assertThat(node(1) in appendedPath,
                        Is(true))
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

            it("returns a path containing that previous node and the new node") {
                assertThat(node(1) in appendedPath,
                        Is(true))
                assertThat(node(2) in appendedPath,
                        Is(true))
            }

            it("keeps the original path with size 1") {
                assertThat(path.size, equalTo(1))
            }
        }

        on("copying the node") {

            val pathCopy = path.copy()

            it("returns a new path instance") {
                assertThat(path !== pathCopy,
                        Is(true))
            }

            it("returns a path equal to the initial path") {
                assertThat(pathCopy, equalTo(path))
            }

            it("returns a path equal to the initial path") {
                assertThat(pathCopy, equalTo(path))
            }
        }

    }

    given("two paths with the same size and the same nodes in the exact same order") {

        val path1 = pathOf(node(1), node(2))
        val path2 = pathOf(node(1), node(2))

        it("is true that the two are equal") {
            assertThat(path1, equalTo(path2))
        }

    }

    given("two paths with different sizes and matching nodes") {

        val path1 = pathOf(node(1), node(2), node(3))
        val path2 = pathOf(node(1), node(2))

        it("is true that the two are different") {
            assertThat(path1, not(equalTo(path2)))
        }

    }

    given("two paths with the same size but different nodes") {

        val path1 = pathOf(node(1), node(2))
        val path2 = pathOf(node(2), node(3))

        it("is true that the two are different") {
            assertThat(path1, not(equalTo(path2)))
        }

    }

    given("two paths with the same size and the same nodes in different order") {

        val path1 = pathOf(node(1), node(2))
        val path2 = pathOf(node(2), node(1))

        it("is true that the two are different") {
            assertThat(path1, not(equalTo(path2)))
        }

    }

    context("obtaining the sub-path before some node") {

        given("path is empty") {

            val path = emptyPath()

            on("obtaining sub-path before some node") {

                val subPath = path.subPathBefore(node(1))

                it("returns empty path") {
                    assertThat(subPath,
                            Is(emptyPath()))
                }
            }
        }

        given("path contains only node 1") {

            val path = pathOf(node(1))

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

        given("path with nodes 1, 2, 3, 4, and 5") {

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

                it("returns path with node 1") {
                    assertThat(subPath,
                            Is(pathOf(node(1))))
                }
            }

            on("obtaining sub-path before node 3") {

                val subPath = path.subPathBefore(node(3))

                it("returns path with nodes 1 and 2") {
                    assertThat(subPath,
                            Is(pathOf(node(1), node(2))))
                }
            }

            on("obtaining sub-path before node 4") {

                val subPath = path.subPathBefore(node(4))

                it("returns path with nodes 1, 2, and 3") {
                    assertThat(subPath,
                            Is(pathOf(node(1), node(2), node(3))))
                }
            }

            on("obtaining sub-path before node 5") {

                val subPath = path.subPathBefore(node(5))

                it("returns path with nodes 1, 2, 3, and 4") {
                    assertThat(subPath,
                            Is(pathOf(node(1), node(2), node(3), node(4))))
                }
            }

            on("obtaining sub-path before node 6") {

                val subPath = path.subPathBefore(node(6))

                it("returns original path with nodes 1, 2, 3, 4, and 5") {
                    assertThat(subPath,
                            Is(pathOf(node(1), node(2), node(3), node(4), node(5))))
                }
            }
        }

        given("path contains nodes 1 and 1") {

            val path = pathOf(node(1), node(1))

            on("obtaining sub-path before node 1") {

                val subPath = path.subPathBefore(node(1))

                it("returns empty path") {
                    assertThat(subPath,
                            Is(emptyPath()))
                }
            }
        }

        given("path contains nodes 2, 1, 3, 1") {

            val path = pathOf(node(2), node(1), node(3), node(1))

            on("obtaining sub-path before node 1") {

                val subPath = path.subPathBefore(node(1))

                it("returns path with node 2") {
                    assertThat(subPath,
                            Is(pathOf(node(2))))
                }
            }
        }
    }


})