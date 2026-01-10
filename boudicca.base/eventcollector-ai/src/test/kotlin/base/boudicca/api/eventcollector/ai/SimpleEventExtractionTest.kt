package base.boudicca.api.eventcollector.ai

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import base.boudicca.api.eventcollector.ai.model.EventData
import base.boudicca.api.eventcollector.ai.service.BoudiccaAiService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan

@SpringBootTest
class SimpleEventExtractionTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = ["base.boudicca.api.eventcollector.ai"])
    class TestConfig

    @Autowired
    private lateinit var boudiccaAiService: BoudiccaAiService

    @Test
    fun `extract event dates with AI service should work`() {
        val events = boudiccaAiService.extractEventList(
            """
            Mai 2026

            Freitag, 1. Mai 2026

            Steyr Lokalbahn ab:   10.30   17.30
            Grünburg ab:              08.30   15.30

            Zuglok am 1.  Mai 2026:
            Barwagen in jedem Zug.

            Rot gekennzeichnete Abfahrten sind bereits für Gruppen ausgebucht!
        """.trimIndent(),
        )

        printEvents(events)

        // for this test we don't really care about the name of the event as in this case the eventcollector will override it anyway
        assertAll {
            assertThat(events.size).isEqualTo(4)
            assertThat(events.filter { it.location?.contains("Steyr Lokalbahn") ?: false }.size).isEqualTo(2)
            assertThat(events.filter { it.location?.contains("Grünburg") ?: false }.size).isEqualTo(2)

            events.forEach { event ->
                assertThat(event.date).isEqualTo("2026-05-01")
            }
        }
    }

    @Test
    fun `extract event dates with AI service should work for complex case`() {
        val events = boudiccaAiService.extractEventList(
            """
            August 2026

            Samstag, 01., 08., 15., 22 und 29. August 2026
            Sonntag, 02., 09., 16., 23. und 30. August 2026

            Steyr Lokalbahn ab:   14.00   18.30
            Grünburg ab:              11.00   17.00

            Zuglok am 01. und 02. August 2026:
            Barwagen in jedem Zug.

            Rot gekennzeichnete Abfahrten sind bereits für Gruppen ausgebucht!
        """.trimIndent(),
        )

        printEvents(events)

        assertThat(events.size).isEqualTo(40)
        assertThat(events.map { it.date }).containsOnly(
            "2026-08-01",
            "2026-08-02",
            "2026-08-08",
            "2026-08-09",
            "2026-08-15",
            "2026-08-16",
            "2026-08-22",
            "2026-08-23",
            "2026-08-29",
            "2026-08-30",
        )
    }

    @Test
    fun `extract event dates with AI service should work with complex indirect ranges - case 1`() {
        // Ferlacher Nikolausdampfzüge
        // https://web.archive.org/web/20250808135041/https://nostalgiebahn.at/nikolauszuege.html
        val events = boudiccaAiService.extractEventList(
            """Betriebstage
Am Samstag und Sonntag, 22. und 23. November 2025 sowie

am Freitag, Samstag und Sonntag, 28., 29. und 30. November 2025


Fahrplan
Abfahrten am Bahnhof Ferlach: 12:45* Uhr, 15:00 Uhr, 17:15 Uhr

* Der 12:45-Uhr-Zug verkehrt nicht am Freitag, dem 28. November 2025!
        """.trimIndent(),
        )

        printEvents(events)

        assertThat(events.size).isEqualTo(14)
        assertThat(events.map { it.date }).containsOnly(
            "2025-11-22",
            "2025-11-23",
            "2025-11-28",
            "2025-11-29",
            "2025-11-30",
        )
    }


    @Test
    fun `extract event dates with AI service should work with complex indirect ranges - case 2`() {
        // Rosentaler Nostalgiezüge, Nostalgieerlebnis Rosental
        // https://web.archive.org/web/20250709064424/https://www.nostalgiebahn.at/rosentaler-dampfzuege.html
        val events = boudiccaAiService.extractEventList(
            """Betriebstage
Jeden Samstag und Sonntag vom 05. Juli bis zum 07. September 2025


Fahrplan
Abfahrten am Bahnhof Weizelsdorf:

11:00 Uhr, 14:00 Uhr

Abfahrten am Bahnhof Ferlach:

13:00 Uhr, 16:00* Uhr

* Ausschließlich Rückfahrt zum Bahnhof Weizelsdorf!
        """.trimIndent(),
        )

        printEvents(events)

        assertThat(events.size).isEqualTo(80)
        println(
            events.map {
                it.date
            }.toSet(),
        )
        assertThat(events.map { it.date }).containsOnly(
            "2025-07-05",
            "2025-07-06",
            "2025-07-12",
            "2025-07-13",
            "2025-07-19",
            "2025-07-20",
            "2025-07-26",
            "2025-07-27",
            "2025-08-02",
            "2025-08-03",
            "2025-08-09",
            "2025-08-10",
            "2025-08-16",
            "2025-08-17",
            "2025-08-23",
            "2025-08-24",
            "2025-08-30",
            "2025-08-31",
            "2025-09-06",
            "2025-09-07",
        )
    }


    @Test
    fun `extract event dates with AI service should work with complex indirect ranges - case 3`() {
        // Panoramazug "Carnica Draisinenexpress"
        // https://web.archive.org/web/20250620083242/https://nostalgiebahn.at/carnica-draisinenexpress.html
        val events = boudiccaAiService.extractEventList(
            """Betriebstage
Jeden Donnerstag vom 03. Juli bis zum 04. September 2025.


Fahrplan und Zustiegsmöglichkeiten
ZUG 1
 	Abfahrt
Bhf. Feistritz	09:30
Bhf. Weizelsdorf *)	10:00
 	Ankunft
Ferlach HISTORAMA	10:25
Eintritt im Museum HISTORAMA im Zeitraum von ca. 10:25 bis 11:45 Uhr im Fahrplan vorgesehen. Rückkehr in Weizelsdorf um 12:10 *) Uhr bzw. in Feistritz um 12:40 Uhr.

*)   Im Bahnhof Weizelsdorf: Anschluss an die S-Bahn aus/nach Klagenfurt (öffentliche Anreise bzw. Abreise möglich)
        """.trimIndent(),
        )

        printEvents(events)

        assertThat(events.size).isEqualTo(30)
        println(
            events.map {
                it.date
            }.toSet(),
        )
        // assertThat(events.map { it.date }).containsOnly(
        //     "2026-08-01",
        //     "2026-08-02",
        //     "2026-08-08",
        //     "2026-08-09",
        //     "2026-08-15",
        //     "2026-08-16",
        //     "2026-08-22",
        //     "2026-08-23",
        //     "2026-08-29",
        //     "2026-08-30",
        // )
    }

    private fun printEvents(events: List<EventData>) {
        events.forEach { event ->
            println("Event: ${event.name}, Location: ${event.location}, Date: ${event.date}, Start Time: ${event.startTime}")
        }
    }

}
