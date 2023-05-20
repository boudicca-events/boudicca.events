package events.boudicca.search.query

import events.boudicca.search.model.Event

@FunctionalInterface
interface Evaluator {
    fun evaluate(event: Event): Boolean
}