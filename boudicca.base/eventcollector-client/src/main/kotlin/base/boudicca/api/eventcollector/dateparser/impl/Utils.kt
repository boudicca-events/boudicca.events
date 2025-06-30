package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.DatePair
import base.boudicca.api.eventcollector.dateparser.DateParserResult
import java.time.OffsetDateTime
import java.time.ZoneId

internal object Utils {

    fun buildSingleDateParserResultFromComponents(components: List<SolutionComponent>): DateParserResult? {
        return buildOffsetDateTimeFromComponents(components)?.run { DateParserResult(listOf(DatePair(this))) }
    }

    fun buildOffsetDateTimeFromComponents(components: List<SolutionComponent>): OffsetDateTime? {
        if (components.any { !it.isSolved() }) {
            return null
        }
        if (components.size == 1) {
            val component = components[0]
            if (component is Date) {
                val localDate = component.toLocalDate()
                val date = localDate.atStartOfDay().atZone(ZoneId.of("Europe/Vienna")) //TODO make configurable
                    .toOffsetDateTime()
                return date
            }
        }
        if (components.size == 2) {
            val dates = components.filterIsInstance<Date>()
            val times = components.filterIsInstance<Time>()
            if (dates.size == 1 && times.size == 1) {
                val localDate = dates[0].toLocalDate()
                val localTime = times[0].toLocalTime()
                val date = localDate.atTime(localTime).atZone(ZoneId.of("Europe/Vienna")) //TODO make configurable
                    .toOffsetDateTime()
                return date
            }
        }
        return null
    }

}
