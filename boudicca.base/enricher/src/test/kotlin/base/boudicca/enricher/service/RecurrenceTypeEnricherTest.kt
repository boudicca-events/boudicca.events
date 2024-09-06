package base.boudicca.enricher.service

import base.boudicca.SemanticKeys
import base.boudicca.model.Event
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class RecurrenceTypeEnricherTest {

    @Test
    fun testSimple() {
        val enriched = callEnrich(
            listOf(
                //group 1 should be detected as recurring
                Event("group 1", OffsetDateTime.now().plusDays(1), mapOf()),
                Event("group 1", OffsetDateTime.now().plusDays(2), mapOf()),
                Event("group 1", OffsetDateTime.now().plusDays(3), mapOf()),
                Event("group 1", OffsetDateTime.now().plusDays(4), mapOf(SemanticKeys.RECURRENCE_TYPE to "RARELY")),
                Event("group 1", OffsetDateTime.now().plusDays(5), mapOf(SemanticKeys.RECURRENCE_TYPE to "willbeoverwritten")),
                //group 2 should NOT be detected as recurring
                Event("group 2", OffsetDateTime.now().plusDays(6), mapOf()),
                Event("group 2", OffsetDateTime.now().plusDays(7), mapOf(SemanticKeys.RECURRENCE_TYPE to "RARELY")),
            )
        )
        assertEquals("REGULARLY", enriched[0].data[SemanticKeys.RECURRENCE_TYPE])
        assertEquals("REGULARLY", enriched[1].data[SemanticKeys.RECURRENCE_TYPE])
        assertEquals("REGULARLY", enriched[2].data[SemanticKeys.RECURRENCE_TYPE])
        assertEquals("RARELY", enriched[3].data[SemanticKeys.RECURRENCE_TYPE])
        assertEquals("REGULARLY", enriched[4].data[SemanticKeys.RECURRENCE_TYPE])
        assertEquals(null, enriched[5].data[SemanticKeys.RECURRENCE_TYPE])
        assertEquals("RARELY", enriched[6].data[SemanticKeys.RECURRENCE_TYPE])
    }

    private fun callEnrich(events: List<Event>): List<Event> {
        return RecurrenceEnricher().enrich(events.map { it.toStructuredEvent() }).map { it.toFlatEvent() }
    }
}