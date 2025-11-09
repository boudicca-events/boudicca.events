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
                    SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3"),
                    SemanticKeys.TAGS to emptyList(),
                    SemanticKeys.TYPE to emptyList(),
                    SemanticKeys.CONCERT_GENRE to emptyList()
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
                    SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3"),
                    SemanticKeys.TAGS to emptyList(),
                    SemanticKeys.TYPE to emptyList(),
                    SemanticKeys.CONCERT_GENRE to emptyList()
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
                    SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3"),
                    SemanticKeys.TAGS to emptyList(),
                    SemanticKeys.TYPE to emptyList(),
                    SemanticKeys.CONCERT_GENRE to emptyList()
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
                    SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3"),
                    SemanticKeys.TAGS to emptyList(),
                    SemanticKeys.TYPE to emptyList(),
                    SemanticKeys.CONCERT_GENRE to emptyList()
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

class ListOfEventWithDifferentNameToBeSearchable : ArgumentsProvider {
    override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(
                listOf(
                    Event("Musical Event In Innenstadt", OffsetDateTime.now(), generateEventData()),
                    Event("Sport Event at JKU", OffsetDateTime.now(), generateEventData()),
                    Event("Cultural Event at Posthof", OffsetDateTime.now(), generateEventData())
                ),
                mapOf(
                    SemanticKeys.LOCATION_NAME to listOf("Location1", "Location2", "Location3"),
                    SemanticKeys.LOCATION_CITY to listOf("City1", "City2", "City3"),
                    SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3"),
                    SemanticKeys.TAGS to emptyList(),
                    SemanticKeys.TYPE to emptyList(),
                    SemanticKeys.CONCERT_GENRE to emptyList()
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

class ListOfFilterableEvents : ArgumentsProvider {
    override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(
                listOf(
                    Event("Musical Event In Innenstadt", OffsetDateTime.now(), musicalEventData()),
                    Event("Sport Event at JKU", OffsetDateTime.now(), sportEventData()),
                    Event("Cultural Event at Posthof", OffsetDateTime.now(), culturalEventData())
                ),
                mapOf(
                    SemanticKeys.LOCATION_NAME to listOf("Theater", "Cinema", "Sport Complex"),
                    SemanticKeys.LOCATION_CITY to listOf("Linz", "Graz", "Wien"),
                    SemanticKeys.CONCERT_BANDLIST to listOf("Imagine Dragons", "Beatles", "Metallica"),
                    SemanticKeys.TAGS to emptyList(),
                    SemanticKeys.TYPE to emptyList(),
                    SemanticKeys.CONCERT_GENRE to emptyList()
                )
            )
        )
    }

    private fun musicalEventData(): Map<String, String> {
        val data = mutableMapOf<String, String>()
        val url = "https://www.event.page.at/musical-event"

        data[SemanticKeys.URL] = url
        data[SemanticKeys.DESCRIPTION] = "long description of musical event"
        data[SemanticKeys.LOCATION_NAME] = "Theater"
        data[SemanticKeys.LOCATION_URL] = "https://www.event.page.at/theater"
        data[SemanticKeys.LOCATION_CITY] = "Linz"
        data[SemanticKeys.SOURCES] = url
        data[SemanticKeys.CONCERT_BANDLIST] = "Imagine Dragons"

        return data
    }

    private fun sportEventData(): Map<String, String> {
        val data = mutableMapOf<String, String>()
        val url = "https://www.event.page.at/sport-event"

        data[SemanticKeys.DESCRIPTION] = "long description of a cinema"
        data[SemanticKeys.LOCATION_NAME] = "Cinema"
        data[SemanticKeys.LOCATION_URL] = "https://www.event.page.at/cinema"
        data[SemanticKeys.LOCATION_CITY] = "Graz"
        data[SemanticKeys.SOURCES] = url

        return data
    }

    private fun culturalEventData(): Map<String, String> {
        val data = mutableMapOf<String, String>()
        val url = "https://www.event.page.at/cultural-event"

        data[SemanticKeys.DESCRIPTION] = "long description of a sport complex"
        data[SemanticKeys.LOCATION_NAME] = "Sport Complex"
        data[SemanticKeys.LOCATION_URL] = "https://www.event.page.at/sport"
        data[SemanticKeys.LOCATION_CITY] = "Wien"
        data[SemanticKeys.SOURCES] = url

        return data
    }
}
