package io

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.jupiter.api.Assertions.assertThrows
import java.io.StringReader
import org.hamcrest.Matchers.`is` as Is


object KeyValueParserTest: Spek({

    given("an empty file") {

        val fileContent = ""
        val handler: KeyValueParser.Handler = mock()

        on("parsing the file") {

            var exception: ParseException? = null

            it("throws a ParseException") {
                KeyValueParser(StringReader(fileContent)).use {
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

    listOf(
            "node = 10" isParsedTo Entry(key = "node", values = listOf("10")),
            "node = 10 | 11" isParsedTo Entry(key = "node", values = listOf("10", "11")),
            "node = 10 | 11 - 9" isParsedTo Entry(key = "node", values = listOf("10", "11 - 9")),
            "node = 10 | 11|abc|1a " isParsedTo Entry(key = "node", values = listOf("10", "11", "abc", "1a")),
            "node = 10 | " isParsedTo Entry(key = "node", values = listOf("10", "")),
            "node = 10 | | 11" isParsedTo Entry(key = "node", values = listOf("10", "", "11")),
            "node = " isParsedTo Entry(key = "node", values = listOf("")),
            "node = | | |" isParsedTo Entry(key = "node", values = listOf("", "", "", "")),
            "node = | a | |" isParsedTo Entry(key = "node", values = listOf("", "a", "", "")),
            "node = a=b" isParsedTo Entry(key = "node", values = listOf("a=b")),
            "node = a = b" isParsedTo Entry(key = "node", values = listOf("a = b")),
            "node = a | a=b" isParsedTo Entry(key = "node", values = listOf("a", "a=b")),
            "node 1 = a" isParsedTo Entry(key = "node 1", values = listOf("a")),
            "node ; a = a" isParsedTo Entry(key = "node ; a", values = listOf("a")),
            "node | a = a" isParsedTo Entry(key = "node | a", values = listOf("a")),
            "node | a = a | b | c" isParsedTo Entry(key = "node | a", values = listOf("a", "b", "c"))
    ).forEach { (line, entry) ->

        given("file with single line `$line`") {

            val handler: KeyValueParser.Handler = mock()

            on("parsing the file") {

                KeyValueParser(StringReader(line)).use {
                    it.parse(handler)
                }

                it("parsed a single entry") {
                    verify(handler, times(1)).onEntry(any(), any())
                }

                it("parsed an entry in line 1 with key '${entry.key}' and values ${entry.values}") {
                    verify(handler, times(1)).onEntry(entry, 1)
                }
            }
        }
    }

    given("file with lines `node = 10`, `link = 10 | 11`") {

        val fileContent = lines(
                "node = 10",
                "link = 10 | 11"
        )
        val handler: KeyValueParser.Handler = mock()

        on("parsing the file") {

            KeyValueParser(StringReader(fileContent)).use {
                it.parse(handler)
            }

            it("parsed a 2 entries") {
                verify(handler, times(2)).onEntry(any(), any())
            }

            it("parsed an entry in line 1 with key 'node' and values [10]") {
                verify(handler, times(1)).onEntry(Entry("node", listOf("10")), 1)
            }

            it("parsed an entry in line 2 with key 'link' and values [10, 11]") {
                verify(handler, times(1)).onEntry(Entry("link", listOf("10", "11")), 2)
            }
        }
    }

    given("file with lines `node = 10`, `link = 10 | 11`, ``") {

        val fileContent = lines(
                "node = 10",
                "link = 10 | 11",
                ""
        )
        val handler: KeyValueParser.Handler = mock()

        on("parsing the file") {

            KeyValueParser(StringReader(fileContent)).use {
                it.parse(handler)
            }

            it("parsed a 2 entries") {
                verify(handler, times(2)).onEntry(any(), any())
            }

            it("parsed an entry in line 1 with key 'node' and values [10]") {
                verify(handler, times(1)).onEntry(Entry("node", listOf("10")), 1)
            }

            it("parsed an entry in line 2 with key 'link' and values [10]") {
                verify(handler, times(1)).onEntry(Entry("link", listOf("10", "11")), 2)
            }
        }
    }

    given("file with lines `node = 10`, ``, `link = 10 | 11`") {

        val fileContent = lines(
                "node = 10",
                "",
                "link = 10 | 11"
        )
        val handler: KeyValueParser.Handler = mock()

        on("parsing the file") {

            KeyValueParser(StringReader(fileContent)).use {
                it.parse(handler)
            }

            it("parsed a 2 entries") {
                verify(handler, times(2)).onEntry(any(), any())
            }

            it("parsed an entry in line 1 with key 'node' and values [10]") {
                verify(handler, times(1)).onEntry(Entry("node", listOf("10")), 1)
            }

            it("parsed an entry in line 3 with key 'link' and values [10, 11]") {
                verify(handler, times(1)).onEntry(Entry("link", listOf("10", "11")), 3)
            }
        }
    }

    listOf(
            "node",
            "node is very good",
            "node | a | b",
            "node : a ",
            "node - a "
    ).forEach { line ->

        given("file with invalid line `$line`") {

            val handler: KeyValueParser.Handler = mock()

            on("parsing the file") {

                var exception: ParseException? = null

                it("throws a ParseException") {
                    KeyValueParser(StringReader(line)).use {
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

typealias Entry = KeyValueParser.Entry


infix fun String.isParsedTo(entry: Entry): Pair<String, Entry> {
    return Pair(this, entry)
}


fun lines(vararg lines: String): String = lines.joinToString("\n")
