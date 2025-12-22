package base.boudicca.query

import base.boudicca.model.structured.KeyFilter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun interface Expression {
    override fun toString(): String
}

abstract class HasOneChild(
    private val name: String,
    private val child: Expression,
) : Expression {
    fun getChild(): Expression = child

    override fun toString(): String = "$name($child)"
}

abstract class HasTwoChildren(
    private val name: String,
    private val leftChild: Expression,
    private val rightChild: Expression,
) : Expression {
    fun getLeftChild(): Expression = leftChild

    fun getRightChild(): Expression = rightChild

    override fun toString(): String = "$name($leftChild,$rightChild)"
}

abstract class FieldAndTextExpression(
    private val name: String,
    keyFilter: String,
    private val text: String,
) : Expression {
    private val keyFilter = parseKeyFilter(keyFilter)

    fun getKeyFilter(): KeyFilter = keyFilter

    fun getText(): String = text

    override fun toString(): String = "$name('${keyFilter.toKeyString()}','$text')"
}

abstract class FieldAndNumberExpression(
    private val name: String,
    keyFilter: String,
    private val number: Number,
) : Expression {
    private val keyFilter = parseKeyFilter(keyFilter)

    fun getKeyFilter(): KeyFilter = keyFilter

    fun getNumber(): Number = number

    override fun toString(): String = "$name('${keyFilter.toKeyString()}',$number)"
}

abstract class FieldExpression(
    private val name: String,
    keyFilter: String,
) : Expression {
    private val keyFilter = parseKeyFilter(keyFilter)

    fun getKeyFilter(): KeyFilter = keyFilter

    override fun toString(): String = "$name('${keyFilter.toKeyString()}')"
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
        } catch (_: DateTimeParseException) {
            throw QueryException("date in wrong format $dateText")
        }
    }

    fun getDate(): LocalDate = date

    override fun toString(): String = "$name('${getKeyFilter().toKeyString()}','${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}')"
}

abstract class AbstractDurationExpression(
    private val name: String,
    startDateKeyFilter: String,
    endDateKeyFilter: String,
    private val duration: Number,
) : Expression {
    private val startDateKeyFilter = parseKeyFilter(startDateKeyFilter)
    private val endDateKeyFilter = parseKeyFilter(endDateKeyFilter)

    fun getStartDateKeyFilter(): KeyFilter = startDateKeyFilter

    fun getEndDateKeyFilter(): KeyFilter = endDateKeyFilter

    fun getDuration(): Number = duration

    override fun toString(): String = "$name('${startDateKeyFilter.toKeyString()}','${endDateKeyFilter.toKeyString()}',$duration)"
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

class IsInNextSecondsExpression(
    keyFilter: String,
    number: Number,
) : FieldAndNumberExpression("ISINNEXTSECONDS", keyFilter, number)

class IsInLastSecondsExpression(
    keyFilter: String,
    number: Number,
) : FieldAndNumberExpression("ISINLASTSECONDS", keyFilter, number)
