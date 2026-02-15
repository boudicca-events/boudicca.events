package base.boudicca.api.eventcollector.annotations

import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class BoudiccaEventCollector(
    val collectorTypeName: String,
)
