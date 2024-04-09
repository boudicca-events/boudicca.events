package base.boudicca.publisher.event.html.testdata

import base.boudicca.SemanticKeys
import base.boudicca.model.Event
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.time.OffsetDateTime
import java.util.stream.Stream

class A11YTestData: ArgumentsProvider {
  override fun provideArguments(p0: ExtensionContext?): Stream<Arguments> {
    return Stream.of(Arguments.of(
      listOf(
        Event("event1", OffsetDateTime.now(), mapOf()),
        Event("event2", OffsetDateTime.now(), mapOf()),
        Event("event3", OffsetDateTime.now(), mapOf()),
        Event("event4", OffsetDateTime.now(), mapOf()),
        Event("event5", OffsetDateTime.now(), mapOf()),
        Event("event6", OffsetDateTime.now(), mapOf()),
        Event("event7", OffsetDateTime.now(), mapOf()),
        Event("event8", OffsetDateTime.now(), mapOf()),
        Event("event9", OffsetDateTime.now(), mapOf()),
        Event("event10", OffsetDateTime.now(), mapOf()),
        Event("event11", OffsetDateTime.now(), mapOf()),
        Event("event12", OffsetDateTime.now(), mapOf()),
        Event("event13", OffsetDateTime.now(), mapOf()),
        Event("event14", OffsetDateTime.now(), mapOf()),
        Event("event15", OffsetDateTime.now(), mapOf()),
        Event("event16", OffsetDateTime.now(), mapOf()),
        Event("event17", OffsetDateTime.now(), mapOf()),
        Event("event18", OffsetDateTime.now(), mapOf()),
        Event("event19", OffsetDateTime.now(), mapOf()),
        Event("event20", OffsetDateTime.now(), mapOf()),
        Event("event21", OffsetDateTime.now(), mapOf()),
        Event("event22", OffsetDateTime.now(), mapOf()),
        Event("event23", OffsetDateTime.now(), mapOf()),
        Event("event24", OffsetDateTime.now(), mapOf()),
        Event("event25", OffsetDateTime.now(), mapOf()),
        Event("event26", OffsetDateTime.now(), mapOf()),
        Event("event27", OffsetDateTime.now(), mapOf()),
        Event("event28", OffsetDateTime.now(), mapOf()),
        Event("event29", OffsetDateTime.now(), mapOf()),
        Event("event30", OffsetDateTime.now(), mapOf()),
      ),
      mapOf(
        SemanticKeys.LOCATION_NAME to listOf("Location1", "Location2", "Location3"),
        SemanticKeys.LOCATION_CITY to listOf("City1", "City2", "City3"),
        SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3")
      )
    )
    )
  }
}
