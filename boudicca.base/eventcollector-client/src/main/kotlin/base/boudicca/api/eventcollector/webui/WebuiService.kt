package base.boudicca.api.eventcollector.webui

import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventcollector.collections.FullCollection
import base.boudicca.api.eventcollector.collections.HttpCall
import base.boudicca.api.eventcollector.collections.SingleCollection
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.HtmlUtils
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private const val CUTOFF_TO_HIGHER_UNIT = 5L

@Suppress("SpringJavaInjectionPointsAutowiringInspection") //this bean is added dynamically
@Service
class WebuiService(
    @Qualifier("eventCollectors") private val eventCollectors: List<EventCollector>,
    @Value("\${boudicca.collector.webui.timeZoneId:Europe/Vienna}") timeZoneId: String
) {

    private val zoneId = ZoneId.of(timeZoneId)

    fun getIndexData(): Map<String, Any> {
        val data = mutableMapOf<String, Any>()
        val fullCollection = Collections.getCurrentFullCollection()
        data["hasOngoingFullCollection"] = fullCollection != null

        if (fullCollection != null) {
            data["fullCollection"] = mapFullCollectionToFrontEnd(fullCollection)
            data["logs"] = formatLogLines(fullCollection.logLines)
        }

        data["fullCollections"] = Collections.getAllPastCollections().map { mapFullCollectionToFrontEnd(it) }

        return data
    }

    fun getSingleCollectionData(uuid: UUID): Map<String, Any> {
        val singleCollection = requireNotNull(findSingleCollection(uuid)) {
            "unable to find single collection with uuid $uuid"
        }

        val data = mutableMapOf<String, Any>()
        data["singleCollection"] = mapSingleCollectionToFrontend(singleCollection)
        data["httpCalls"] = singleCollection.httpCalls
            .sortedBy { it.startTime }
            .map {
                mapHttpCallToFrontend(it)
            }
        data["logs"] = formatLogLines(singleCollection.logLines)

        return data
    }

    fun getFullCollectionData(uuid: UUID): Map<String, Any> {
        val fullCollection = requireNotNull(findFullCollection(uuid)) {
            "unable to find full collection with uuid $uuid"
        }

        val data = mutableMapOf<String, Any>()
        data["fullCollection"] = mapFullCollectionToFrontEnd(fullCollection)
        data["logs"] = formatLogLines(fullCollection.logLines)

        return data
    }

    private fun mapFullCollectionToFrontEnd(fullCollection: FullCollection): Map<String, *> {
        val singleCollections = fullCollection.singleCollections.associateBy { it.collectorName }

        return mapOf(
            "id" to fullCollection.id.toString(),
            "duration" to formatDuration(fullCollection.startTime, fullCollection.endTime),
            "startEndTime" to formatStartEndTime(fullCollection.startTime, fullCollection.endTime),
            "errorCount" to fullCollection.getTotalErrorCount(),
            "warningCount" to fullCollection.getTotalWarningCount(),
            "hasErrors" to (fullCollection.getTotalErrorCount() > 0),
            "hasWarnings" to (fullCollection.getTotalWarningCount() > 0),
            "totalEventsCollected" to fullCollection.singleCollections.sumOf { it.totalEventsCollected },
            "singleCollections" to
                    eventCollectors
                        .map { it.getName() }
                        .sorted()
                        .map {
                            mapSingleCollectionToFrontend(it, singleCollections[it])
                        }
        )
    }

    private fun mapSingleCollectionToFrontend(it: SingleCollection): Map<String, *> {
        return mapSingleCollectionToFrontend(it.collectorName, it)
    }

    private fun mapSingleCollectionToFrontend(name: String, it: SingleCollection?): Map<String, *> {
        if (it != null) {
            return mapOf(
                "id" to it.id.toString(),
                "name" to HtmlUtils.htmlEscape(it.collectorName),
                "duration" to formatDuration(it.startTime, it.endTime),
                "startEndTime" to formatStartEndTime(it.startTime, it.endTime),
                "errorCount" to it.errorCount.toString(),
                "warningCount" to it.warningCount.toString(),
                "hasErrors" to (it.errorCount > 0),
                "hasWarnings" to (it.warningCount > 0),
                "totalEventsCollected" to (it.totalEventsCollected).toString(),
            )
        } else {
            return mapOf(
                "id" to null,
                "name" to name,
                "duration" to "-",
                "startEndTime" to "-",
                "errorCount" to "-",
                "warningCount" to "-",
                "hasErrors" to false,
                "hasWarnings" to false,
                "totalEventsCollected" to "-",
            )
        }
    }

    private fun mapHttpCallToFrontend(httpCall: HttpCall): Map<String, String> {
        return mapOf(
            "url" to httpCall.url!!,
            "responseCode" to if (httpCall.responseCode == 0) "-" else httpCall.responseCode.toString(),
            "duration" to formatDuration(httpCall.startTime, httpCall.endTime),
            "startEndTime" to formatStartEndTime(httpCall.startTime, httpCall.endTime),
            "postData" to (httpCall.postData ?: ""),
        )
    }

    private fun findSingleCollection(id: UUID): SingleCollection? {
        return Collections.getAllPastCollections()
            .union(listOfNotNull(Collections.getCurrentFullCollection()))
            .flatMap { it.singleCollections }
            .find { it.id == id }
    }

    private fun findFullCollection(id: UUID): FullCollection? {
        return Collections.getAllPastCollections().find { it.id == id }
    }

    private fun formatStartEndTime(startTimeInMillis: Long, endTimeInMillis: Long): String {
        val startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeInMillis), zoneId)
        val formattedStartTime = DateTimeFormatter.ofPattern("d.M.uu HH:mm").format(startTime)

        if (endTimeInMillis == 0L) {
            return formattedStartTime +
                    " / ..."
        }

        val endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimeInMillis), zoneId)
        return if (startTime.toLocalDate().equals(endTime.toLocalDate())) {
            formattedStartTime +
                    " / " +
                    DateTimeFormatter.ofPattern("HH:mm").format(endTime)
        } else {
            formattedStartTime +
                    " / " +
                    DateTimeFormatter.ofPattern("d.M.uu HH:mm").format(endTime)
        }
    }

    private fun formatDuration(startTime: Long, endTime: Long): String {
        val realEndTime = if (endTime != 0L) endTime else System.currentTimeMillis()
        val durationInMillis = realEndTime - startTime
        val duration = Duration.ofMillis(durationInMillis)
        return if (duration < Duration.ofSeconds(CUTOFF_TO_HIGHER_UNIT)) {
            "${duration.toMillis()} ms"
        } else if (duration < Duration.ofMinutes(CUTOFF_TO_HIGHER_UNIT)) {
            "${duration.toSeconds()} s"
        } else {
            "${duration.toMinutes()} m"
        }
    }

    private fun formatLogLines(logLines: List<String>): String =
        HtmlUtils.htmlEscape(logLines.joinToString("\n"))

}
