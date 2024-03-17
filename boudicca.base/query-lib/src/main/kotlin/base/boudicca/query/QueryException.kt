package base.boudicca.query

class QueryException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
    constructor(message: String?) : this(message, null)
    constructor(cause: Throwable?) : this(null, cause)
}