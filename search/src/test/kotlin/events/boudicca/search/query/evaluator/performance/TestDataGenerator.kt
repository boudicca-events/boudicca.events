package events.boudicca.search.query.evaluator.performance

import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.EventPublisherResourceApi
import events.boudicca.search.util.Utils
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import kotlin.random.Random

const val WANTED_EVENTS = 100_000
fun main() {
    val startTime = System.currentTimeMillis()
    val testData = TestDataGenerator.getTestData()
    val out = ObjectOutputStream(FileOutputStream("testdata.dump", false))
    out.writeObject(testData)
    out.close()
    println("generating and saving test data took ${System.currentTimeMillis() - startTime}ms")
}

object TestDataGenerator {

    fun getTestData(): Collection<Map<String, String>> {
        val apiClient = ApiClient()
        apiClient.updateBaseUri("https://api.boudicca.events")
        val eventPublisherResourceApi = EventPublisherResourceApi(apiClient)

        val originalEvents = eventPublisherResourceApi.eventsGet()
        val originalEventsMapped = originalEvents.filterNotNull().map { Utils.mapEventToMap(it) }

        val remixEvents = remixEvents(originalEventsMapped)
        return remixEvents
    }

    /**
     * takes events + remixes them to generate a big amount of test data
     */
    private fun remixEvents(events: List<Map<String, String>>): Collection<Map<String, String>> {
        val remixesNeeded = WANTED_EVENTS - events.size
        if (remixesNeeded < 0) {
            return events
        }
        return generateRemixes(events, remixesNeeded)
    }

    private fun generateRemixes(
        events: List<Map<String, String>>,
        remixesNeeded: Int
    ): Collection<Map<String, String>> {
        val metadata = generateMetaData(events)

        val remixes = mutableListOf<Map<String, String>>()
        for (i in 1..remixesNeeded) {
            remixes.add(generateRemix(metadata))
        }
        return events.plus(remixes)
    }

    private fun generateRemix(metadata: Map<String, Metadata>): Map<String, String> {
        val remix = mutableMapOf<String, String>()
        for (field in metadata.entries) {
            if (field.value.percentage > Math.random()) {
                val value = StringBuilder()
                for (i in 1..(Random.Default.nextInt(field.value.min, field.value.max + 1))) {
                    value.append(field.value.words.random())
                    value.append(" ")
                }
                remix[field.key] = value.toString()
            }
        }
        return remix
    }

    /**
     * metadata is for each field chance of existing, all possible words, min and max amount of words
     */
    private fun generateMetaData(events: List<Map<String, String>>): Map<String, Metadata> {
        val allFields = events.flatMap { it.keys }.toSet()
        return allFields.associate { Pair(it, generateMetaData(events, it)) }
    }

    private fun generateMetaData(events: List<Map<String, String>>, field: String): Metadata {
        var min = Int.MAX_VALUE
        var max = Int.MIN_VALUE
        var count = 0
        val words = mutableSetOf<String>()

        events
            .mapNotNull { it[field] }
            .forEach {
                val fieldWords = it
                    .split(" ", "\t", "\n", "\r\n")
                    .filter { it != null && it.isNotBlank() } //meh, good enough for this

                count++
                words.addAll(fieldWords)
                min = Math.min(min, fieldWords.size)
                max = Math.max(max, fieldWords.size)
            }
        return Metadata(count.toFloat() / events.size.toFloat(), words.toList(), min, max)
    }

    private data class Metadata(
        val percentage: Float,
        val words: List<String>,
        val min: Int,
        val max: Int,
    )
}