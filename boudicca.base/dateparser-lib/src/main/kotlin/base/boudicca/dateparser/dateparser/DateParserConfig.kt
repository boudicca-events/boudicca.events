package base.boudicca.dateparser.dateparser

import java.time.ZoneId

data class DateParserConfig(
    val timezone: ZoneId = ZoneId.of("Europe/Vienna"),
    val alwaysPrintDebugTracing: Boolean = false
)
