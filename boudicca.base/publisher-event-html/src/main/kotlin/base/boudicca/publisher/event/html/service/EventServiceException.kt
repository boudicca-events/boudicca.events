package base.boudicca.publisher.event.html.service

class EventServiceException(
    message: String?, cause: Throwable?, val showToUser: Boolean
) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null, false)
    constructor(cause: Throwable?) : this(null, cause, false)
}