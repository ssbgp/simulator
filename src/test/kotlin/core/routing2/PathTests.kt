package core.routing2

import testing2.node
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
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
            MatcherAssert.assertThat(path.contains(node(1)), Matchers.`is`(true))
        }

        it("contains node 2") {
            MatcherAssert.assertThat(path.contains(node(2)), Matchers.`is`(true))
        }

        it("does not contain node 3") {
            MatcherAssert.assertThat(path.contains(node(3)), Matchers.`is`(false))
        }

    }

    given("an empty path") {

        val emptyPath = emptyPath()

        it("has size 0") {
            MatcherAssert.assertThat(emptyPath.size, Matchers.equalTo(0))
        }

        on("appending a new node") {

            val appendedPath = emptyPath.append(node(1))

            it("returns a path of size 1") {
                MatcherAssert.assertThat(appendedPath.size, Matchers.equalTo(1))
            }

            it("returns a path containing that new node") {
                MatcherAssert.assertThat(node(1) in appendedPath, Matchers.`is`(true))
            }

            it("keeps the original path empty") {
                MatcherAssert.assertThat(emptyPath.size, Matchers.equalTo(0))
            }

        }

    }

    given("a path with 1 node") {

        val path = pathOf(node(1))

        it("has size 1") {
            MatcherAssert.assertThat(path.size, Matchers.equalTo(1))
        }

        on("appending a new node") {

            val appendedPath = path.append(node(2))

            it("returns a path of size 2") {
                MatcherAssert.assertThat(appendedPath.size, Matchers.equalTo(2))
            }

            it("returns a path containing that previous node and the new node") {
                MatcherAssert.assertThat(node(1) in appendedPath, Matchers.`is`(true))
                MatcherAssert.assertThat(node(2) in appendedPath, Matchers.`is`(true))
            }

            it("keeps the original path with size 1") {
                MatcherAssert.assertThat(path.size, Matchers.equalTo(1))
            }
        }

        on("copying the node") {

            val pathCopy = path.copy()

            it("returns a new path instance") {
                MatcherAssert.assertThat(path !== pathCopy, Matchers.`is`(true))
            }

            it("returns a path equal to the initial path") {
                MatcherAssert.assertThat(pathCopy, Matchers.equalTo(path))
            }

            it("returns a path equal to the initial path") {
                MatcherAssert.assertThat(pathCopy, Matchers.equalTo(path))
            }
        }

    }

    given("two paths with the same size and the same nodes in the exact same order") {

        val path1 = pathOf(node(1), node(2))
        val path2 = pathOf(node(1), node(2))

        it("is true that the two are equal") {
            MatcherAssert.assertThat(path1, Matchers.equalTo(path2))
        }

    }

    given("two paths with different sizes and matching nodes") {

        val path1 = pathOf(node(1), node(2), node(3))
        val path2 = pathOf(node(1), node(2))

        it("is true that the two are different") {
            MatcherAssert.assertThat(path1, Matchers.not(Matchers.equalTo(path2)))
        }

    }

    given("two paths with the same size but different nodes") {

        val path1 = pathOf(node(1), node(2))
        val path2 = pathOf(node(2), node(3))

        it("is true that the two are different") {
            MatcherAssert.assertThat(path1, Matchers.not(Matchers.equalTo(path2)))
        }

    }

    given("two paths with the same size and the same nodes in different order") {

        val path1 = pathOf(node(1), node(2))
        val path2 = pathOf(node(2), node(1))

        it("is true that the two are different") {
            MatcherAssert.assertThat(path1, Matchers.not(Matchers.equalTo(path2)))
        }

    }

    context("obtaining the sub-path before some node") {

        given("path is empty") {

            val path = emptyPath()

            on("obtaining sub-path before some node") {

                val subPath = path.subPathBefore(node(1))

                it("returns empty path") {
                    MatcherAssert.assertThat(subPath, Matchers.`is`(emptyPath()))
                }
            }
        }

        given("path contains only node 1") {

            val path = pathOf(node(1))

            on("obtaining sub-path before node 1") {

                val subPath = path.subPathBefore(node(1))

                it("returns empty path") {
                    MatcherAssert.assertThat(subPath, Matchers.`is`(emptyPath()))
                }
            }

            on("obtaining sub-path before node 2") {

                val subPath = path.subPathBefore(node(2))

                it("returns original path with node 1") {
                    MatcherAssert.assertThat(subPath, Matchers.`is`(path))
                }
            }
        }

        given("path with nodes 1, 2, 3, 4, and 5") {

            val path = pathOf(node(1), node(2), node(3), node(4), node(5))

            on("obtaining sub-path before node 1") {

                val subPath = path.subPathBefore(node(1))

                it("returns empty path") {
                    MatcherAssert.assertThat(subPath, Matchers.`is`(emptyPath()))
                }
            }

            on("obtaining sub-path before node 2") {

                val subPath = path.subPathBefore(node(2))

                it("returns path with node 1") {
                    MatcherAssert.assertThat(subPath, Matchers.`is`(pathOf(node(1))))
                }
            }

            on("obtaining sub-path before node 3") {

                val subPath = path.subPathBefore(node(3))

                it("returns path with nodes 1 and 2") {
                    MatcherAssert.assertThat(subPath, Matchers.`is`(pathOf(node(1), node(2))))
                }
            }

            on("obtaining sub-path before node 4") {

                val subPath = path.subPathBefore(node(4))

                it("returns path with nodes 1, 2, and 3") {
                    MatcherAssert.assertThat(subPath, Matchers.`is`(pathOf(node(1), node(2), node(3))))
                }
            }

            on("obtaining sub-path before node 5") {

                val subPath = path.subPathBefore(node(5))

                it("returns path with nodes 1, 2, 3, and 4") {
                    MatcherAssert.assertThat(subPath, Matchers.`is`(pathOf(node(1), node(2), node(3), node(4))))
                }
            }

            on("obtaining sub-path before node 6") {

                val subPath = path.subPathBefore(node(6))

                it("returns original path with nodes 1, 2, 3, 4, and 5") {
                    MatcherAssert.assertThat(subPath, Matchers.`is`(pathOf(node(1), node(2), node(3), node(4), node(5))))
                }
            }
        }

        given("path contains nodes 1 and 1") {

            val path = pathOf(node(1), node(1))

            on("obtaining sub-path before node 1") {

                val subPath = path.subPathBefore(node(1))

                it("returns empty path") {
                    MatcherAssert.assertThat(subPath, Matchers.`is`(emptyPath()))
                }
            }
        }

        given("path contains nodes 2, 1, 3, 1") {

            val path = pathOf(node(2), node(1), node(3), node(1))

            on("obtaining sub-path before node 1") {

                val subPath = path.subPathBefore(node(1))

                it("returns path with node 2") {
                    MatcherAssert.assertThat(subPath, Matchers.`is`(pathOf(node(2))))
                }
            }
        }
    }


})