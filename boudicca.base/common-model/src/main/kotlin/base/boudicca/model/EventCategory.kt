package base.boudicca.model

import java.util.*

/**
 * current event categories we support, with all their known event types
 */
enum class EventCategory(val types: Set<String>) {
    //ALL and OTHER are special cases needing special care
    ALL(emptySet()),
    OTHER(emptySet()),
    MUSIC(setOf("konzert", "concert", "alternative", "singer/songwriter", "party", "songwriter/alternative", "musik")),
    ART(
        setOf(
            "kabarett",
            "theater",
            "wissenskabarett",
            "provinzkrimi",
            "comedy",
            "figurentheater",
            "film",
            "visual comedy",
            "tanz",
            "performance",
            "musiklesung",
            "literatur",
            "museen",
            "museum",
            "ausstellung",
            "musical",
            "brauchtum",
        )
    ),
    TECH(setOf("techmeetup", "technology", "technologie")),
    SPORT(setOf("sport", "football", "soccer"));

    companion object {
        fun getForType(type: String?): EventCategory? {
            if (type == null) {
                return null
            }
            val lowerType = type.lowercase(Locale.getDefault())
            for (category in entries) {
                for (categoryType in category.types) {
                    if (lowerType.contains(categoryType)) {
                        return category
                    }
                }
            }
            return null
        }
    }
}