package ui.cli

/**
 * Created on 30-08-2017
 *
 * @author David Fialho
 */
class Console {

    companion object {
        private val format = "%s: %s"
    }

    fun info(message: String) {
        println(String.format(format, "INFO", message))
    }

    fun warning(message: String) {
        println(String.format(format, "WARNING", message))
    }

    fun error(message: String) {
        System.err.println(String.format(format, "ERROR", message))
    }
}