package events.boudicca.model

import java.time.OffsetDateTime

data class Event(
    var name: String,
    var startDate: OffsetDateTime,
    var endDate: OffsetDateTime? = null,
    var url: String? = null,
    var type: String? = null,
    var description: String? = null,
    var tags: List<String>? = null,
    var registration: RegistrationEnum? = null,
    var location: Location? = Location(),
    var concert: Concert? = Concert(),
    var additionalData: Map<String, String>? = HashMap(),
)