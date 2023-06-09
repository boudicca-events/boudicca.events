package events.boudicca.search.query.evaluator.performance

import events.boudicca.SemanticKeys
import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.EventPublisherResourceApi
import events.boudicca.search.util.Utils
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable
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

    fun getTestData(): Pair<Collection<Map<String, String>>, Map<String, Metadata>> {
        val apiClient = ApiClient()
        apiClient.updateBaseUri("https://api.boudicca.events")
        val eventPublisherResourceApi = EventPublisherResourceApi(apiClient)

        val originalEvents = eventPublisherResourceApi.eventsGet()
        val originalEventsMapped = originalEvents.filterNotNull().map { Utils.mapEventToMap(it) }

        return remixEvents(originalEventsMapped)
    }

    /**
     * takes events + remixes them to generate a big amount of test data
     */
    private fun remixEvents(events: List<Map<String, String>>): Pair<Collection<Map<String, String>>, Map<String, Metadata>> {
        val metadata = generateMetaData(events)

        val remixes = mutableListOf<Map<String, String>>()
        for (i in 1..(WANTED_EVENTS - events.size)) {
            remixes.add(generateRemix(metadata))
        }
        return Pair(events.plus(remixes), metadata)
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
                remix[field.key] = value.toString().trim()
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
        var words = mutableSetOf<String>()

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
        if (field == SemanticKeys.LOCATION_NAME) {
            max = 3
            words = words.take(5).toMutableSet()
        }
        return Metadata(count.toFloat() / events.size.toFloat(), words.toList(), min, max)
    }

    data class Metadata(
        val percentage: Float,
        val words: List<String>,
        val min: Int,
        val max: Int,
    ) : Serializable
}