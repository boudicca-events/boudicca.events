package base.boudicca.enricher.service

import base.boudicca.SemanticKeys
import base.boudicca.model.Event
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class RecurrenceEnricherTest {

    @Test
    fun testSimple() {
        val enriched = RecurrenceEnricher().enrich(
            listOf(
                //group 1 should be detected as recurring
                Event("group 1", OffsetDateTime.now().plusDays(1), mapOf()),
                Event("group 1", OffsetDateTime.now().plusDays(2), mapOf()),
                Event("group 1", OffsetDateTime.now().plusDays(3), mapOf()),
                Event("group 1", OffsetDateTime.now().plusDays(4), mapOf(SemanticKeys.RECURRENCE to "something")),
                //group 2 should NOT be detected as recurring
                Event("group 2", OffsetDateTime.now().plusDays(5), mapOf()),
                Event("group 2", OffsetDateTime.now().plusDays(6), mapOf(SemanticKeys.RECURRENCE to "something")),
            )
        )
        assertEquals("recurring", enriched[0].data[SemanticKeys.RECURRENCE])
        assertEquals("recurring", enriched[1].data[SemanticKeys.RECURRENCE])
        assertEquals("recurring", enriched[2].data[SemanticKeys.RECURRENCE])
        assertEquals("something", enriched[3].data[SemanticKeys.RECURRENCE])
        assertEquals(null, enriched[4].data[SemanticKeys.RECURRENCE])
        assertEquals("something", enriched[5].data[SemanticKeys.RECURRENCE])
    }
}