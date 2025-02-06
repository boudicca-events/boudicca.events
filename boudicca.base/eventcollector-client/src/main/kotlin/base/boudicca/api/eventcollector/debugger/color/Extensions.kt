package base.boudicca.api.eventcollector.debugger.color

/**
 * this code is licensed under apache-2 license from https://github.com/ziggy42/kolor
 * because the package is only published to jcenter https://github.com/ziggy42/kolor/issues/15
 * until it becomes available in mvcentral we have to copy the important parts.
 */

fun String.red() = TerminalColor.foreground(this, Color.RED)

fun String.green() = TerminalColor.foreground(this, Color.GREEN)

fun String.yellow() = TerminalColor.foreground(this, Color.YELLOW)

fun String.blue() = TerminalColor.foreground(this, Color.BLUE)
