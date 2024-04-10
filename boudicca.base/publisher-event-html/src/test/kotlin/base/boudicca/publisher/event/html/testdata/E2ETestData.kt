package base.boudicca.publisher.event.html.testdata

import base.boudicca.SemanticKeys
import base.boudicca.model.Event
import base.boudicca.publisher.event.html.util.buildEventList
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.stream.Stream

class E2EGeneralTestData : ArgumentsProvider {
  override fun provideArguments(p0: ExtensionContext?): Stream<Arguments> {
    return Stream.of(
      Arguments.of(
        buildEventList(30),
        mapOf(
          SemanticKeys.LOCATION_NAME to listOf("Location1", "Location2", "Location3"),
          SemanticKeys.LOCATION_CITY to listOf("City1", "City2", "City3"),
          SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3")
        )
      )
    )
  }
}

class E2ESingleEventTestData : ArgumentsProvider {
  override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
    return Stream.of(
      Arguments.of(
        listOf(
          Event(
            "Musical Event in Innenstadt",
            OffsetDateTime.of(2023, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC),
            generateEventData()
          )
        ),
        mapOf(
          SemanticKeys.LOCATION_NAME to listOf("Location1", "Location2", "Location3"),
          SemanticKeys.LOCATION_CITY to listOf("City1", "City2", "City3"),
          SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3")
        )
      )
    )
  }

  private fun generateEventData(): Map<String, String> {
    val data = mutableMapOf<String, String>()
    data[SemanticKeys.URL] = "https://www.event.page.at/"
    data[SemanticKeys.DESCRIPTION] = "long description"
    data[SemanticKeys.LOCATION_NAME] = "Theater"
    data[SemanticKeys.LOCATION_URL] = "https://www.event.page.at/location"
    data[SemanticKeys.LOCATION_CITY] = "Linz"
    data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!

    return data
  }
}

class E2ESingleEventWithoutURL : ArgumentsProvider {
  override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
    return Stream.of(
      Arguments.of(
        listOf(
          Event(
            "Musical Event in Innenstadt",
            OffsetDateTime.of(2023, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC),
            generateEventData()
          )
        ),
        mapOf(
          SemanticKeys.LOCATION_NAME to listOf("Location1", "Location2", "Location3"),
          SemanticKeys.LOCATION_CITY to listOf("City1", "City2", "City3"),
          SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3")
        )
      )
    )
  }

  private fun generateEventData(): Map<String, String> {
    val data = mutableMapOf<String, String>()
    data[SemanticKeys.DESCRIPTION] = "long description"
    data[SemanticKeys.LOCATION_NAME] = "Theater"
    data[SemanticKeys.LOCATION_URL] = "https://www.event.page.at/location"
    data[SemanticKeys.LOCATION_CITY] = "Linz"
    data[SemanticKeys.SOURCES] = ""

    return data
  }
}

class SingleEventWithA11YInformation : ArgumentsProvider {
  override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
    return Stream.of(
      Arguments.of(
        listOf(
          Event(
            "Musical Event in Innenstadt",
            OffsetDateTime.of(2023, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC),
            generateEventData()
          )
        ),
        mapOf(
          SemanticKeys.LOCATION_NAME to listOf("Location1", "Location2", "Location3"),
          SemanticKeys.LOCATION_CITY to listOf("City1", "City2", "City3"),
          SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3")
        )
      )
    )
  }

  private fun generateEventData(): Map<String, String> {
    val data = mutableMapOf<String, String>()
    data[SemanticKeys.DESCRIPTION] = "long description"
    data[SemanticKeys.LOCATION_NAME] = "Theater"
    data[SemanticKeys.LOCATION_URL] = "https://www.event.page.at/location"
    data[SemanticKeys.LOCATION_CITY] = "Linz"
    data[SemanticKeys.SOURCES] = ""
    data[SemanticKeys.ACCESSIBILITY_ACCESSIBLEENTRY] = "true"
    data[SemanticKeys.ACCESSIBILITY_ACCESSIBLESEATS] = "true"
    data[SemanticKeys.ACCESSIBILITY_ACCESSIBLETOILETS] = "true"

    return data
  }
}
