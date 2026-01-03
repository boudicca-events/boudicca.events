package base.boudicca.api.eventcollector.annotations

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors")
annotation class BoudiccaEventCollector(
    @get:AliasFor(annotation = ConditionalOnProperty::class, attribute = "name")
    val value: String,
)
