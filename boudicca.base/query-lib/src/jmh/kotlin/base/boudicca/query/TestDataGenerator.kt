package base.boudicca.query

import base.boudicca.SemanticKeys
import base.boudicca.api.eventdb.publisher.EventDbPublisherClient
import base.boudicca.model.Entry
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.Serializable
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.writeBytes
import kotlin.math.min
import kotlin.random.Random

const val WANTED_EVENTS = 100_000
fun main() {
    val startTime = System.currentTimeMillis()
    val testData = TestDataGenerator.getTestData()

    writeTestData(testData)

    println("generating and saving test data took ${System.currentTimeMillis() - startTime}ms")
}

private fun writeTestData(testData: Pair<List<Map<String, String>>, Map<String, TestDataGenerator.Metadata>>) {
    val objectMapper = JsonMapper.builder().addModule(JavaTimeModule())
        .addModule(KotlinModule.Builder().build()).build()

    val bytes = objectMapper.writeValueAsBytes(testData.first)
    Path.of("C:\\projects\\boudicca\\testdata.dump")
        .writeBytes(bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
}

object TestDataGenerator {

    fun getTestData(): Pair<List<Map<String, String>>, Map<String, Metadata>> {
        val publisherClient = EventDbPublisherClient("https://eventdb.boudicca.events")

        val originalEvents = publisherClient.getAllEntries()

        return remixEvents(originalEvents.toList())
    }

    /**
     * takes events + remixes them to generate a big amount of test data
     */
    private fun remixEvents(events: List<Entry>): Pair<List<Map<String, String>>, Map<String, Metadata>> {
        val metadata = generateMetaData(events)

        val remixes = mutableListOf<Map<String, String>>()
        for (i in 1..WANTED_EVENTS) {
            remixes.add(generateRemix(metadata))
        }
        return Pair(remixes, metadata)
    }

    private fun generateRemix(metadata: Map<String, Metadata>): Map<String, String> {
        val remix = mutableMapOf<String, String>()
        for (field in metadata.entries) {
            if (field.value.percentage > Math.random()) {
                val value = StringBuilder()
                val maxDistance = field.value.median - field.value.min
                for (i in 1..(field.value.median + maxDistance - Random.Default.nextInt(0, maxDistance * 2 + 1))) {
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
        return allFields.associateWith { generateMetaData(events, it) }
    }

    private fun generateMetaData(events: List<Map<String, String>>, field: String): Metadata {
        var min = Int.MAX_VALUE
        var count = 0
        var words = mutableSetOf<String>()
        val wordCounts = mutableListOf<Int>()

        events
            .mapNotNull { it[field] }
            .forEach {
                val fieldWords = it
                    .split(" ", "\t", "\n", "\r\n")
                    .filter(String::isNotBlank) //meh, good enough for this

                count++
                words.addAll(fieldWords)
                min = min(min, fieldWords.size)
                wordCounts.add(fieldWords.size)
            }
        if (field == SemanticKeys.LOCATION_NAME) {
            words = words.take(5).toMutableSet()
        }
        return Metadata(
            count.toFloat() / events.size.toFloat(), words.toList(), min,
            wordCounts.sorted()[wordCounts.size / 2]
        )
    }

    data class Metadata(
        val percentage: Float,
        val words: List<String>,
        val min: Int,
        val median: Int,
    ) : Serializable
}