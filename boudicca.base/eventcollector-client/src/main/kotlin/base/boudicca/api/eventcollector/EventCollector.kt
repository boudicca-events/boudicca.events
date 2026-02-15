package base.boudicca.api.eventcollector

import base.boudicca.api.eventcollector.annotations.BoudiccaEventCollector
import base.boudicca.api.eventcollector.config.EventCollectorBaseConfig
import base.boudicca.model.Event
import base.boudicca.model.structured.StructuredEvent
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Base interface of EventCollectors
 * @param T type of the eventcollector config class
 */
abstract class EventCollector<T : EventCollectorBaseConfig>(
    // unfortunately this KClass is needed as parameter, because otherwise the type information is lost at runtime
    open val configClass: KClass<T>,
) {
    lateinit var config: T

    open fun getName(): String {
        val annotation = this::class.findAnnotation<BoudiccaEventCollector>()
        requireNotNull(annotation) { "Every EventCollector must be annotated with @BoudiccaEventCollector" }
        return annotation.collectorTypeName
    }

    /**
     * Will take a name and generic properties and instantiate an instance of the config class for the eventcollector
     * so that in the collector the config can be used typesafe again.
     *
     * In case of type mismatch the initialization process should fail here.
     */
    fun configure(
        name: String? = null,
        properties: Map<String, String> = emptyMap(),
    ) {
        // the name is a special case, so it can be overridden in the config without having to declare an array for each eventcollector
        val mutableProps = properties.toMutableMap()
        val nameOverride = name ?: getName()
        mutableProps["name"] = nameOverride

        // ✨✨ magic ✨✨
        val binder = Binder(MapConfigurationPropertySource(mutableProps))
        val typeSafeConfig = binder.bind("", Bindable.of(configClass.java)).get()

        config = typeSafeConfig
    }

    /**
     * required for directly setting the config in the debugger
     */
    fun withDebugConfig(config: T): EventCollector<T> {
        this.config = config
        return this
    }

    open fun collectEvents(): List<Event> = collectStructuredEvents().map { it.toFlatEvent() }

    open fun collectStructuredEvents(): List<StructuredEvent> = emptyList()
}
