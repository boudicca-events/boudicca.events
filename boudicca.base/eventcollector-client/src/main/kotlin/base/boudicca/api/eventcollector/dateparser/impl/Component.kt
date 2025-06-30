package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.impl.Guesser.GuesserType


internal sealed interface Component {
    fun isSolved(): Boolean
}

internal data class Separator(val weight: Int) : Component {
    override fun isSolved(): Boolean {
        return true
    }
}

internal data class Any(val value: String, val possibleTypes: Set<GuesserType>) : Component {
    override fun isSolved(): Boolean {
        return possibleTypes.size <= 1
    }
}
