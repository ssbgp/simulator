package io

import com.nhaarman.mockito_kotlin.*
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.jupiter.api.Assertions.assertThrows
import java.io.StringReader
import org.hamcrest.Matchers.`is` as Is

object TopologyParserTest: Spek({

    given("an empty file") {

        val fileContent = ""
        val handler: TopologyParser.Handler = mock()

        on("parsing the file") {

            var exception: ParseException? = null

            it("throws a ParseException") {
                TopologyParser(StringReader(fileContent), handler).use {
                    exception = assertThrows(ParseException::class.java) {
                        it.parse()
                    }
                }
            }

            it("indicates the error is in line 1") {
                assertThat(exception?.lineNumber, Is(1))
            }
        }
    }

    given("file with single line `node = 10`") {

        val fileContent = "node = 10"
        val handler: TopologyParser.Handler = mock()

        on("parsing the file") {

            TopologyParser(StringReader(fileContent), handler).use {
                it.parse()
            }

            it("parsed a single node") {
                verify(handler, times(1)).onNodeItem(any(), any(), any())
            }

            it("did NOT parse any link") {
                verify(handler, never()).onLinkItem(any(), any(), any(), any())
            }

            it("parsed node with ID 10 and no values in line 1") {
                verify(handler, times(1)).onNodeItem(10, emptyList(), 1)
            }
        }
    }

    given("file with single line with multiple values `node = 10 | 11`") {

        val fileContent = "node = 10 | 11"
        val handler: TopologyParser.Handler = mock()

        on("parsing the file") {

            TopologyParser(StringReader(fileContent), handler).use {
                it.parse()
            }

            it("parsed a single node") {
                verify(handler, times(1)).onNodeItem(any(), any(), any())
            }

            it("did NOT parse any link") {
                verify(handler, never()).onLinkItem(any(), any(), any(), any())
            }

            it("parsed node with ID 10 and values [11] in line 1") {
                verify(handler, times(1)).onNodeItem(10, listOf("11"), 1)
            }
        }
    }

    given("file with single line with multiple values `node = 10 | 11 - 19`") {

        val fileContent = "node = 10 | 11 - 19"
        val handler: TopologyParser.Handler = mock()

        on("parsing the file") {

            TopologyParser(StringReader(fileContent), handler).use {
                it.parse()
            }

            it("parsed a single node") {
                verify(handler, times(1)).onNodeItem(any(), any(), any())
            }

            it("did NOT parse any link") {
                verify(handler, never()).onLinkItem(any(), any(), any(), any())
            }

            it("parsed node with values [11 - 19] in line 1") {
                verify(handler, times(1)).onNodeItem(10, listOf("11 - 19"), 1)
            }
        }
    }

    given("file with single line with multiple values `node = 10 | 11 | abc | 1a`") {

        val fileContent = "node = 10 | 11 | abc | 1a"
        val handler: TopologyParser.Handler = mock()

        on("parsing the file") {

            TopologyParser(StringReader(fileContent), handler).use {
                it.parse()
            }

            it("parsed a single node") {
                verify(handler, times(1)).onNodeItem(any(), any(), any())
            }

            it("did NOT parse any link") {
                verify(handler, never()).onLinkItem(any(), any(), any(), any())
            }

            it("parsed node with ID 10 and values [11, abc, 1a] in line 1") {
                verify(handler, times(1)).onNodeItem(10, listOf("11", "abc", "1a"), 1)
            }
        }
    }

    given("file with single line `link = 10 | 11`") {

        val fileContent = "link = 10 | 11"
        val handler: TopologyParser.Handler = mock()

        on("parsing the file") {

            TopologyParser(StringReader(fileContent), handler).use {
                it.parse()
            }

            it("parsed a single link") {
                verify(handler, times(1)).onLinkItem(any(), any(), any(), any())
            }

            it("did NOT parse any node") {
                verify(handler, never()).onNodeItem(any(), any(), any())
            }

            it("parsed in line 1 a link from node 10 to node 11 with no values") {
                verify(handler, times(1)).onLinkItem(10, 11, emptyList(), 1)
            }
        }
    }

    given("file with single line `link = 10 | 11 | abc`") {

        val fileContent = "link = 10 | 11 | abc"
        val handler: TopologyParser.Handler = mock()

        on("parsing the file") {

            TopologyParser(StringReader(fileContent), handler).use {
                it.parse()
            }

            it("parsed a single link") {
                verify(handler, times(1)).onLinkItem(any(), any(), any(), any())
            }

            it("did NOT parse any node") {
                verify(handler, never()).onNodeItem(any(), any(), any())
            }

            it("parsed in line 1 a link from node 10 to node 11 with values [abc]") {
                verify(handler, times(1)).onLinkItem(10, 11, listOf("abc"), 1)
            }
        }
    }

    fun lines(vararg lines: String): String = lines.joinToString("\n")

    given("file with lines `node = 10`, `link = 10 | 11`") {

        val fileContent = lines(
                "node = 10",
                "link = 10 | 11"
        )
        val handler: TopologyParser.Handler = mock()

        on("parsing the file") {

            TopologyParser(StringReader(fileContent), handler).use {
                it.parse()
            }

            it("parsed in line 1 a node with ID 10 and no values") {
                verify(handler, times(1)).onNodeItem(10, emptyList(), 1)
            }

            it("parsed in line 2 a link from node 10 to node 11 with no values") {
                verify(handler, times(1)).onLinkItem(10, 11, emptyList(), 2)
            }
        }
    }

    given("file with lines `node = 10`, `link = 10 | 11`, ``") {

        val fileContent = lines(
                "node = 10",
                "link = 10 | 11",
                ""
        )
        val handler: TopologyParser.Handler = mock()

        on("parsing the file") {

            TopologyParser(StringReader(fileContent), handler).use {
                it.parse()
            }

            it("parsed in line 1 a node with ID 10 and no values") {
                verify(handler, times(1)).onNodeItem(10, emptyList(), 1)
            }

            it("parsed in line 2 a link from node 10 to node 11 with no values") {
                verify(handler, times(1)).onLinkItem(10, 11, emptyList(), 2)
            }
        }
    }

    given("file with lines `node = 10`, ``, `link = 10 | 11`") {

        val fileContent = lines(
                "node = 10",
                "",
                "link = 10 | 11"
        )
        val handler: TopologyParser.Handler = mock()

        on("parsing the file") {

            TopologyParser(StringReader(fileContent), handler).use {
                it.parse()
            }

            it("parsed in line 1 a node with ID 10 and no values") {
                verify(handler, times(1)).onNodeItem(10, emptyList(), 1)
            }

            it("parsed in line 3 a link from node 10 to node 11 with no values") {
                verify(handler, times(1)).onLinkItem(10, 11, emptyList(), 3)
            }
        }
    }

    val incorrectLines = listOf(
            "node = a",
            "node = 10a",
            "element = 10",
            "node = 10 = 11",
            "node 10",
            "node = ",
            "node",
            "node == 10",
            "node = 10 | 11 = 19",
            "link = 10",
            "link = 10 | a",
            "link = a | 10",
            "link = a | b"
    )

    incorrectLines.forEach { line ->

        given("file with incorrect line `$line`") {

            val handler: TopologyParser.Handler = mock()

            on("parsing the file") {

                var exception: ParseException? = null

                it("throws a ParseException") {
                    TopologyParser(StringReader(line), handler).use {
                        exception = assertThrows(ParseException::class.java) {
                            it.parse()
                        }
                    }
                }

                it("indicates the error is in line 1") {
                    assertThat(exception?.lineNumber, Is(1))
                }
            }
        }
    }

})