package events.boudicca.search.query.evaluator.performance

import events.boudicca.search.query.ContainsExpression
import events.boudicca.search.query.evaluator.SimpleEvaluator
import java.io.FileInputStream
import java.io.ObjectInputStream

fun main() {
    PerformanceTest.run()
}

object PerformanceTest {

    fun run() {
        val (testData, metadata) = loadTestData()

        runTest(testData, metadata)
    }

    private fun runTest(testData: Collection<Map<String, String>>, metadata: Map<String, TestDataGenerator.Metadata>) {
        val evaluator = SimpleEvaluator(testData)
        var count = 0
        var time = 0L
        for (i in 1..100) {
            val field = metadata.entries.random()
            val expression = ContainsExpression(field.key, field.value.words.random())
            val startTime = System.currentTimeMillis()
            val result = evaluator.evaluate(expression)
            time += System.currentTimeMillis() - startTime
            count += result.size
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