package io

import com.nhaarman.mockito_kotlin.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is` as Is
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.Assertions.assertThrows
import java.io.StringReader

/**
 * Created on 31-08-2017
 *
 * @author David Fialho
 */
object StubParserTest: Spek({

    given("an empty file") {

        val fileContent = ""
        val handler: StubParser.Handler = mock()

        on("parsing the file") {

            it("does not throw anything") {
                StubParser(StringReader(fileContent)).use {
                    it.parse(handler)
                }
            }

            it("does not parse any stub item") {
                verify(handler, never()).onStubLink(any(), any(), any(), any())
            }
        }
    }

    given("file with single line `1 | 2 | C`") {

        val fileContent = "1 | 2 | C"
        val handler: StubParser.Handler = mock()

        on("parsing the file") {

            StubParser(StringReader(fileContent)).use {
                it.parse(handler)
            }

            it("parsed a single stub") {
                verify(handler, times(1)).onStubLink(any(), any(), any(), any())
            }

            it("parsed in line 1 a stub with ID 1, neighbor 2, and extender with label `C`") {
                verify(handler, times(1)).onStubLink(id = 1, inNeighbor = 2, label = "C", currentLine = 1)
            }
        }
    }

    fun lines(vararg lines: String): String = lines.joinToString("\n")

    given("file with single lines `1 | 2 | C`, `1 | 3 | R`") {

        val fileContent = lines(
                "1 | 2 | C",
                "1 | 3 | R"
        )
        val handler: StubParser.Handler = mock()

        on("parsing the file") {

            StubParser(StringReader(fileContent)).use {
                it.parse(handler)
            }

            it("parsed two stubs") {
                verify(handler, times(2)).onStubLink(any(), any(), any(), any())
            }

            it("parsed in line 1 a stub with ID 1, neighbor 2, and extender with label `C`") {
                verify(handler, times(1)).onStubLink(id = 1, inNeighbor = 2, label = "C", currentLine = 1)
            }

            it("parsed in line 2 a stub with ID 1, neighbor 3, and extender with label `R`") {
                verify(handler, times(1)).onStubLink(id = 1, inNeighbor = 3, label = "R", currentLine = 2)
            }
        }
    }

    given("file with single lines `1 | 2 | C`, `  `,  `1 | 3 | R`") {

        val fileContent = lines(
                "1 | 2 | C",
                "  ",
                "1 | 3 | R"
        )
        val handler: StubParser.Handler = mock()

        on("parsing the file") {

            StubParser(StringReader(fileContent)).use {
                it.parse(handler)
            }

            it("parsed two stubs") {
                verify(handler, times(2)).onStubLink(any(), any(), any(), any())
            }

            it("parsed in line 1 a stub with ID 1, neighbor 2, and extender with label `C`") {
                verify(handler, times(1)).onStubLink(id = 1, inNeighbor = 2, label = "C", currentLine = 1)
            }

            it("parsed in line 3 a stub with ID 1, neighbor 3, and extender with label `R`") {
                verify(handler, times(1)).onStubLink(id = 1, inNeighbor = 3, label = "R", currentLine = 3)
            }
        }
    }

    val incorrectLines = listOf(
            "1 | 2 ",
            "1 | 2 |",
            "a | 2 | C",
            "1 | b | C",
            " |  | "
    )

    incorrectLines.forEach { line ->

        given("file with incorrect line `$line`") {

            val handler: StubParser.Handler = mock()

            on("parsing the file") {

                var exception: ParseException? = null

                it("throws a ParseException") {
                    StubParser(StringReader(line)).use {
                        exception = assertThrows(ParseException::class.java) {
                            it.parse(handler)
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