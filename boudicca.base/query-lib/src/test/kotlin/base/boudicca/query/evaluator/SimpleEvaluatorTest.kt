package base.boudicca.query.evaluator

class SimpleEvaluatorTest : AbstractEvaluatorTest() {
    override fun createEvaluator(entries: Collection<Map<String, String>>): Evaluator {
        return SimpleEvaluator(entries)
    }
}

