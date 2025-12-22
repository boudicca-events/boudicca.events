package base.boudicca.query.parsing

class Token(private val type: TokenType, private val token: String? = null, private val number: Number? = null) {
    fun getType(): TokenType = type

    fun getToken(): String? = token

    fun getNumber(): Number? = number
}

enum class TokenType {
    TEXT,
    NUMBER,
    AND,
    OR,
    EQUALS,
    CONTAINS,
    NOT,
    BEFORE,
    AFTER,
    GROUPING_OPEN,
    GROUPING_CLOSE,
    DURATION,
    LONGER,
    SHORTER,
    HAS_FIELD,
    IS_IN_NEXT_SECONDS,
    IS_IN_LAST_SECONDS,
}
