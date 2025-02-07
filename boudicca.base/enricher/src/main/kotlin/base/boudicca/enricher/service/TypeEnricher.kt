package base.boudicca.enricher.service

import base.boudicca.SemanticKeys
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.StructuredEvent
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service

@Service
@Order(EnricherOrderConstants.TypeEnricherOrder)
class TypeEnricher : Enricher {

    override fun enrich(event: StructuredEvent): StructuredEvent {
        val types = event.getProperty(SemanticKeys.TYPE_PROPERTY)
        if (types.isNotEmpty()) {
            val builder = event.toBuilder()
            for (type in types) {
                mapType(builder, type.first, type.second)
            }
            return builder.build()
        } else {
            return event
        }
    }

    private fun mapType(builder: StructuredEvent.StructuredEventBuilder, key: Key, value: String) {
        val lowerType = value.lowercase()
        for (knownMusicType in KNOWN_MUSIC_TYPES) {
            if (lowerType.indexOf(knownMusicType) != -1) {
                builder.withKeyValuePair(key, "concert")
                builder.withProperty(SemanticKeys.CONCERT_GENRE_PROPERTY, value)
                return
            }
        }
    }

    private val KNOWN_MUSIC_TYPES: Set<String> = setOf(
        "metal",
        "indie",
        "pop",
        "jazz",
        "hiphop",
        "rap",
        "rock",
        "electronic",
        "punk",
        "house",
        "brass",
        "reggae",
        "soul",
        "funk",
        "folk",
        "dub",
        "klassik",
    )
}
