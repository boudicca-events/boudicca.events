package base.boudicca.query

import base.boudicca.model.Entry
import base.boudicca.query.evaluator.*
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.openjdk.jmh.annotations.*
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.readBytes


@State(Scope.Benchmark)
open class EvaluatorTest {

//    @Param(
//        """ "name" contains "rock" """,
//        """ "description" contains "rock" """,
//        """ "whatever" contains "rock" """,
//    )
//    var query: String? = null

//    @Param(
//        """ "category" equals "music" """,
//        """ "name" equals "music" """,
//        """ "whatever" equals "music" """,
//    )
//    var query: String? = null

    @Param(
//        """ "category" equals "music" """,
//        """ "name" contains "rock" """,
//        """ "description" contains "rock" """,
        """ "name" equals "music" """,
    )
    var query: String? = null

    var expression: Expression? = null

    @Param(/*"noop", *//*"simple",*/ "optimizing")
    var mode: String? = null

    @Param(/*"5000", "20000",*/ "100000")
    var testDataSize: Int? = null

    var evaluator: Evaluator? = null

    @Setup
    fun setup() {
        expression = BoudiccaQueryRunner.parseQuery(query!!)
        evaluator = when (mode) {
            "noop" -> NoopEvaluator()
            "simple" -> SimpleEvaluator(loadTestData(testDataSize))
            "optimizing" -> OptimizingEvaluator(loadTestData(testDataSize))
            else -> throw IllegalArgumentException("illegal mode $mode")
        }
    }

    @Benchmark
    @Fork(3)
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 2, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 2, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
    fun testEvaluator(): QueryResult {
        return evaluator!!.evaluate(expression!!, PAGE_ALL)
    }
}

fun main() {

//    Thread.sleep(20000)
    val testData: List<Map<String, String>> = loadTestData(100_000)


    val evaluator = OptimizingEvaluator(testData.toList())
//    val evaluator = SimpleEvaluator(testData.toList())


//    val expression = BoudiccaQueryRunner.parseQuery(""" "name" contains "rock" """)
//    val expression = BoudiccaQueryRunner.parseQuery(""" "description" contains "rock" """)
//    val expression = BoudiccaQueryRunner.parseQuery(""" "whatever" contains "rock" """)

//    val expression = BoudiccaQueryRunner.parseQuery(""" "category" equals "music" """)
//    val expression = BoudiccaQueryRunner.parseQuery(""" "name" equals "music" """)
//    val expression = BoudiccaQueryRunner.parseQuery(""" "whatever" equals "music" """)

//    val expression = BoudiccaQueryRunner.parseQuery(""" ("startDate" after "2024-04-12") and (duration "startDate" "endDate" shorter 720.0) and ((not (hasField "recurrence.type")) or ("recurrence.type" equals "ONCE")) and "*" contains "rock" """)
//
//    println("search took:" + measureTime {
//        val queryResult = evaluator.evaluate(expression, PAGE_ALL)
//        println(queryResult.totalResults)
//    })
//    println("second search took:" + measureTime {
//        val queryResult = evaluator.evaluate(expression, PAGE_ALL)
//        println(queryResult.totalResults)
//    })
//    var sum = 0
//    while (sum != 1) {
//        val result = evaluator.evaluate(expression, PAGE_ALL)
//        sum += result.result.hashCode()
//    }
//
//    println(sum)


    val optimizingEvaluator = OptimizingEvaluator(loadTestData(100_000))


    println(
        BoudiccaQueryRunner.evaluateQuery(
            """ "name" equals "music" """,
            PAGE_ALL,
            optimizingEvaluator
        ).totalResults
    )


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