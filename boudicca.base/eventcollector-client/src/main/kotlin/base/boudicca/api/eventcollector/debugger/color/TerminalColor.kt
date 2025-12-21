package base.boudicca.api.eventcollector.debugger.color

/**
 * Object to add color information to strings
 * @author Andrea Pivetta
 *
 * this code is licensed under apache-2 license from https://github.com/ziggy42/kolor
 * because the package is only published to jcenter https://github.com/ziggy42/kolor/issues/15
 * until it becomes available in mvcentral we have to copy the important parts.
 */
object TerminalColor {
    internal const val ESCAPE = '\u001B'
    internal const val RESET = "$ESCAPE[0m"

    /**
     * Create a string that will be printed with the specified color as foreground
     * @param string The string to color
     * @param color The color to use
     * @return The colored string
     */
    fun foreground(string: String, color: Color) = color(string, color.foreground)

    /**
     * Create a string that will be printed with the specified color as background
     * @param string The string to color
     * @param color The color to use
     * @return The colored string
     */
    fun background(string: String, color: Color) = color(string, color.background)

    private fun color(string: String, ansiString: String) = "$ansiString$string$RESET"
}
