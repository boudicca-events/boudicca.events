package base.boudicca.dateparser.dateparser

import java.time.Clock
import java.time.ZoneId

data class DateParserConfig(
    val dayMonthOrder: DayMonthOrder = DayMonthOrder.DAY_MONTH,
    val timezone: ZoneId = ZoneId.of("Europe/Vienna"),
    val alwaysPrintDebugTracing: Boolean = false,
    val clock: Clock = Clock.system(timezone),
) {
    enum class DayMonthOrder {
        DAY_MONTH,
        MONTH_DAY,
    }
}
