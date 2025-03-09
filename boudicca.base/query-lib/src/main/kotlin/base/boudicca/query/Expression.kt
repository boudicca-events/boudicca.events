package base.boudicca.query

import base.boudicca.model.structured.KeyFilter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

interface Expression {
    override fun toString(): String
}

abstract class HasOneChild(
    private val name: String,
    private val child: Expression,
) : Expression {

    fun getChild(): Expression {
        return child
    }

    override fun toString(): String {
        return "$name($child)"
    }
}

abstract class HasTwoChildren(
    private val name: String,
    private val leftChild: Expression,
    private val rightChild: Expression,
) : Expression {

    fun getLeftChild(): Expression {
        return leftChild
    }

    fun getRightChild(): Expression {
        return rightChild
    }

    override fun toString(): String {
        return "$name($leftChild,$rightChild)"
    }
}

abstract class FieldAndTextExpression(
    private val name: String,
    keyFilter: String,
    private val text: String,
) : Expression {

    private val keyFilter = parseKeyFilter(keyFilter)

    fun getKeyFilter(): KeyFilter {
        return keyFilter
    }

    fun getText(): String {
        return text
    }

    override fun toString(): String {
        return "$name('${keyFilter.toKeyString()}','$text')"
    }
}

abstract class FieldExpression(
    private val name: String,
    keyFilter: String,
) : Expression {

    private val keyFilter = parseKeyFilter(keyFilter)

    fun getKeyFilter(): KeyFilter {
        return keyFilter
    }

    override fun toString(): String {
        return "$name('${keyFilter.toKeyString()}')"
    }
}

abstract class DateExpression(
    private val name: String,
    dateKeyFilter: String,
    dateText: String,
) : FieldAndTextExpression(name, dateKeyFilter, dateText) {

    private val date: LocalDate

    init {
        try {
            date = LocalDate.parse(dateText, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            throw QueryException("date in wrong format $dateText")
        }
    }

    fun getDate(): LocalDate {
        return date
    }

    override fun toString(): String {
        return "$name('${getKeyFilter().toKeyString()}','${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}')"
    }
}

abstract class AbstractDurationExpression(
    private val name: String,
    startDateKeyFilter: String,
    endDateKeyFilter: String,
    private val duration: Number,
) : Expression {

    private val startDateKeyFilter = parseKeyFilter(startDateKeyFilter)
    private val endDateKeyFilter = parseKeyFilter(endDateKeyFilter)

    fun getStartDateKeyFilter(): KeyFilter {
        return startDateKeyFilter
    }

    fun getEndDateKeyFilter(): KeyFilter {
        return endDateKeyFilter
    }

    fun getDuration(): Number {
        return duration
    }

    override fun toString(): String {
        return "$name('${startDateKeyFilter.toKeyString()}','${endDateKeyFilter.toKeyString()}',${duration})"
    }
}

private fun parseKeyFilter(keyFilter: String): KeyFilter {
    try {
        return KeyFilter.parse(keyFilter)
    } catch (e: IllegalArgumentException) {
        throw QueryException("invalid keyfilter", e)
    }
}

class AndExpression(
    leftChild: Expression,
    rightChild: Expression,
) : HasTwoChildren("AND", leftChild, rightChild)

class OrExpression(
    leftChild: Expression,
    rightChild: Expression,
) : HasTwoChildren("OR", leftChild, rightChild)

class NotExpression(
    child: Expression,
) : HasOneChild("NOT", child)

class ContainsExpression(
    keyFilter: String,
    text: String,
) : FieldAndTextExpression("CONTAINS", keyFilter, text)

class EqualsExpression(
    keyFilter: String,
    text: String,
) : FieldAndTextExpression("EQUALS", keyFilter, text)

class BeforeExpression(
    dateKeyFilter: String,
    dateText: String,
) : DateExpression("BEFORE", dateKeyFilter, dateText)

class AfterExpression(
    dateKeyFilter: String,
    dateText: String,
) : DateExpression("AFTER", dateKeyFilter, dateText)

class DurationShorterExpression(
    startDateField: String,
    endDateField: String,
    duration: Number,
) : AbstractDurationExpression("DURATIONSHORTER", startDateField, endDateField, duration)

class DurationLongerExpression(
    startDateField: String,
    endDateField: String,
    duration: Number,
) : AbstractDurationExpression("DURATIONLONGER", startDateField, endDateField, duration)

class HasFieldExpression(
    keyFilter: String,
) : FieldExpression("HASFIELD", keyFilter)
