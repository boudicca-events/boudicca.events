package base.boudicca.query.evaluator.util

import base.boudicca.SemanticKeys
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap

class EvaluatorUtilDurationTest {

    @Test
    fun testEmpty() {
        Assertions.assertEquals(
            0.0,
            getDuration(
                "startDate", "endDate",
                mapOf(
                )
            )
        )
    }

    @Test
    fun testNoStart() {
        Assertions.assertEquals(
            0.0,
            getDuration(
                "startDate", "endDate",
                mapOf(
                    SemanticKeys.ENDDATE to "2024-05-31T01:00:00Z",
                )
            )
        )
    }

    @Test
    fun testNoEnd() {
        Assertions.assertEquals(
            0.0,
            getDuration(
                "startDate", "endDate",
                mapOf(
                    SemanticKeys.STARTDATE to "2024-05-31T01:00:00Z",
                )
            )
        )
    }

    @Test
    fun testSimple() {
        Assertions.assertEquals(
            1.0,
            getDuration(
                "startDate", "endDate",
                mapOf(
                    SemanticKeys.STARTDATE to "2024-05-31T00:00:00Z",
                    SemanticKeys.ENDDATE to "2024-05-31T01:00:00Z",
                )
            )
        )
    }

    @Test
    fun testNegative() {
        Assertions.assertEquals(
            -1.0,
            getDuration(
                "startDate", "endDate",
                mapOf(
                    SemanticKeys.STARTDATE to "2024-05-31T01:00:00Z",
                    SemanticKeys.ENDDATE to "2024-05-31T00:00:00Z",
                )
            )
        )
    }

    @Test
    fun testFraction() {
        Assertions.assertEquals(
            0.5,
            getDuration(
                "startDate", "endDate",
                mapOf(
                    SemanticKeys.STARTDATE to "2024-05-31T00:00:00Z",
                    SemanticKeys.ENDDATE to "2024-05-31T00:30:00Z",
                )
            )
        )
    }

    fun getDuration(startDateField: String, endDateField: String, event: Map<String, String>): Double {
        return EvaluatorUtil.getDuration(startDateField, endDateField, event, ConcurrentHashMap())
    }

}