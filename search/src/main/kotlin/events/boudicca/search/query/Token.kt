package events.boudicca.search.query

class Token(
    private val type: TokenType,
    private val token: String?,
) {

    fun getType(): TokenType {
        return type
    }

    fun getToken(): String? {
        return token
    }
}

enum class TokenType {
    TEXT,
    AND,
    OR,
    EQUALS,
    CONTAINS,
    NOT,
    BEFORE,
    AFTER,
    IS,
    GROUPING_OPEN,
    GROUPING_CLOSE,
}
