package base.boudicca.query.evaluator

import java.time.Clock

class SimpleEvaluatorTest : AbstractEvaluatorTest() {
    override fun createEvaluator(entries: Collection<Map<String, String>>, clock: Clock): Evaluator = SimpleEvaluator(entries, clock)
}
