package base.boudicca.query.evaluator

import java.time.Clock


class OptimizingEvaluatorTest : AbstractEvaluatorTest() {
    override fun createEvaluator(entries: Collection<Map<String, String>>, clock: Clock): Evaluator {
        return OptimizingEvaluator(entries, clock)
    }
}

