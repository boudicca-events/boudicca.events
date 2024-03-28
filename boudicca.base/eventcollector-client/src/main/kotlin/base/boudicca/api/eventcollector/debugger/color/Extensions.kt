package base.boudicca.api.eventcollector.debugger.color

fun String.red() = TerminalColor.foreground(this, Color.RED)

fun String.green() = TerminalColor.foreground(this, Color.GREEN)

fun String.yellow() = TerminalColor.foreground(this, Color.YELLOW)

fun String.blue() = TerminalColor.foreground(this, Color.BLUE)