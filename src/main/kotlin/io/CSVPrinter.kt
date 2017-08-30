package io

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.Writer

/**
 * Created on 30-08-2017
 *
 * @author David Fialho
 *
 * This file contains helper methods to work with a CSV printer
 */

/**
 * Creates a printer that will print values to the specified writer following some format.
 *
 * @throws IOException if the optional header cannot be printed.
 */
@Throws(IOException::class)
fun CSVPrinter(writer: Writer): CSVPrinter {
    return CSVPrinter(writer, CSVFormat.EXCEL.withDelimiter(';'))
}

/**
 * Creates a printer that will print values to the specified file following some format.
 *
 * @throws IOException if the optional header cannot be printed.
 */
@Throws(IOException::class)
fun CSVPrinter(file: File): CSVPrinter {
    return CSVPrinter(FileWriter(file, true))
}
