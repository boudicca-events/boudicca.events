package base.boudicca.query.evaluator

class OptimizingEvaluatorTest : AbstractEvaluatorTest() {
    override fun createEvaluator(entries: Collection<Map<String, String>>): Evaluator {
        return OptimizingEvaluator(entries)
    }
}

