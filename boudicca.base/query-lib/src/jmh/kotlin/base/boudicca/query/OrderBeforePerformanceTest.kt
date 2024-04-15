package base.boudicca.query

import base.boudicca.model.Entry
import base.boudicca.query.evaluator.OptimizingEvaluator
import base.boudicca.query.evaluator.PAGE_ALL
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.openjdk.jmh.annotations.*
import java.nio.file.Path
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.time.measureTime


@State(Scope.Benchmark)
open class OrderBeforePerformanceTest {

    @Param("5000", "20000", "100000")
    var testDataSize: Int? = null

    @Param("20", "30", "50", "70", "90")
    var resultIsEveryXItem: Int? = null

    var testData: List<Entry>? = null
    var startDateCache: ConcurrentHashMap<String, OffsetDateTime>? = null

    @Setup
    fun setup() {
        startDateCache = ConcurrentHashMap<String, OffsetDateTime>()
        testData = Utils.order(loadTestData(testDataSize), startDateCache!!)
//        testData = loadTestData(testDataSize)
    }

//    @Benchmark
    @Fork(3)
    @Warmup(iterations = 2, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 2, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
    fun test(): List<Entry> {
        val result = mutableSetOf<Int>()
        var i = 0
        while (i < testData!!.size) {
            result.add(i)
            i += resultIsEveryXItem!!
        }

        return testData!!.filterIndexed { index, _ -> result.contains(index) }
    }
}

private fun loadTestData(testDataSize: Int? = null): List<Map<String, String>> {
//        return listOf(
//            mapOf("name" to "what","description" to "what","test" to "what"),
//            mapOf("name" to "rock","description" to "what","test" to "what"),
//            mapOf("name" to "asd","description" to "rock","test" to "what"),
//            mapOf("name" to "what","description" to "what","test" to "what"),
//        )

    val objectMapper = JsonMapper.builder().addModule(JavaTimeModule())
        .addModule(KotlinModule.Builder().build()).build()

    var path = Path.of("testdata.dump")
    if (!path.exists()) {
        path = Path.of("../../testdata.dump")
    }
    val testData = objectMapper.readValue(
        path.readBytes(),
        object : TypeReference<List<Entry>>() {})


//    val testData = EventDbPublisherClient("https://eventdb.boudicca.events").getAllEntries()

    if (testDataSize != null) {
        return testData.take(testDataSize)
    } else {
        return testData
    }
}