package events.boudicca.search.service.query

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

abstract class TextExpression(
    private val name: String,
    private val text: String,
) : Expression {

    fun getText(): String {
        return text
    }

    override fun toString(): String {
        return "$name('$text')"
    }
}

abstract class NumberExpression(
    private val name: String,
    private val number: Number,
) : Expression {

    fun getNumber(): Number {
        return number
    }

    override fun toString(): String {
        return "$name($number)"
    }
}

abstract class DateExpression(
    private val name: String,
    dateText: String,
) : TextExpression(name, dateText) {

    private val date: LocalDate

    init {
        try {
            date = LocalDate.parse(dateText, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            throw IllegalStateException("date in wrong format $dateText")
        }
    }

    fun getDate(): LocalDate {
        return date
    }

    override fun toString(): String {
        return "$name('${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}')"
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
    text: String,
) : DateExpression("BEFORE", text)

class AfterExpression(
    text: String,
) : DateExpression("AFTER", text)

class IsExpression(
    text: String,
) : TextExpression("IS", text)

class DurationShorterExpression(
    duration: Number,
) : NumberExpression("DURATIONSHORTER", duration)

class DurationLongerExpression(
    duration: Number,
) : NumberExpression("DURATIONLONGER", duration)
