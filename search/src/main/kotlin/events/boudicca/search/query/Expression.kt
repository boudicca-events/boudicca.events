package events.boudicca.search.query

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

abstract class TextExpression(
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
) : TextExpression("CONTAINS", fieldName, text)

class EqualsExpression(
    fieldName: String,
    text: String,
) : TextExpression("EQUALS", fieldName, text)

