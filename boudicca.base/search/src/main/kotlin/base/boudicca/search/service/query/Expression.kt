package base.boudicca.search.service.query

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
    private val fieldName: String,
    private val text: String,
) : Expression {

    fun getFieldName(): String {
        return fieldName
    }

    fun getText(): String {
        return text
    }

    override fun toString(): String {
        return "$name('$fieldName','$text')"
    }
}

abstract class DateExpression(
    private val name: String,
    dateFieldName: String,
    dateText: String,
) : FieldAndTextExpression(name, dateFieldName, dateText) {

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
        return "$name('${getFieldName()}','${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}')"
    }
}

abstract class AbstractDurationExpression(
    private val name: String,
    private val startDateField: String,
    private val endDateField: String,
    private val duration: Number,
) : Expression {

    fun getStartDateField(): String {
        return startDateField
    }

    fun getEndDateField(): String {
        return endDateField
    }

    fun getDuration(): Number {
        return duration
    }

    override fun toString(): String {
        return "$name('${startDateField}','${endDateField}',${duration})"
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
    fieldName: String,
    text: String,
) : FieldAndTextExpression("CONTAINS", fieldName, text)

class EqualsExpression(
    fieldName: String,
    text: String,
) : FieldAndTextExpression("EQUALS", fieldName, text)

class BeforeExpression(
    dateFieldName: String,
    dateText: String,
) : DateExpression("BEFORE", dateFieldName, dateText)

class AfterExpression(
    dateFieldName: String,
    dateText: String,
) : DateExpression("AFTER", dateFieldName, dateText)

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
