package events.boudicca.search.query

import events.boudicca.search.model.Event

interface Evaluator {
    fun evaluate(event: Event): Boolean
}