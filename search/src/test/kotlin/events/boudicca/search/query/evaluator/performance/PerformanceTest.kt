package events.boudicca.search.query.evaluator.performance

import events.boudicca.search.query.ContainsExpression
import events.boudicca.search.query.PAGE_ALL
import events.boudicca.search.query.evaluator.EvaluatorUtil
import events.boudicca.search.query.evaluator.SimpleEvaluator
import java.io.FileInputStream
import java.io.ObjectInputStream
import kotlin.random.Random

fun main() {
    PerformanceTest.run()
}

object PerformanceTest {

    fun run() {
        val (testData, metadata) = loadTestData()

        runTest(testData, metadata)
    }

    private fun runTest(testData: Collection<Map<String, String>>, metadata: Map<String, TestDataGenerator.Metadata>) {
        val random = Random(123)
        val evaluator = SimpleEvaluator(testData.map { EvaluatorUtil.toEvent(it) })
        var count = 0
        var time = 0L
        for (i in 1..100) {
            val field = metadata.entries.random(random)
            val expression = ContainsExpression(field.key, field.value.words.random(random))
            val startTime = System.currentTimeMillis()
            val result = evaluator.evaluate(expression, PAGE_ALL)
            time += System.currentTimeMillis() - startTime
            count += result.totalResults
        }
        println("simple contains took ${time}ms and found $count results")
    }

    private fun loadTestData(): Pair<Collection<Map<String, String>>, Map<String, TestDataGenerator.Metadata>> {
        val startTime = System.currentTimeMillis()
        val inStream = ObjectInputStream(FileInputStream("testdata.dump"))
        val testData = inStream.readObject()
        inStream.close()
        println("loading took ${System.currentTimeMillis() - startTime}ms")
        return testData as Pair<Collection<Map<String, String>>, Map<String, TestDataGenerator.Metadata>>
    }

}