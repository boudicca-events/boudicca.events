package base.boudicca

object SemanticKeys {
    // general properties
    const val NAME = "name"
    const val STARTDATE = "startDate"
    const val ENDDATE = "endDate"
    const val URL = "url"
    const val TYPE = "type"
    const val CATEGORY = "category"
    const val DESCRIPTION = "description"
    const val RECURRENCE_TYPE = "recurrence.type"
    const val RECURRENCE_INTERVAL = "recurrence.interval"
    const val TAGS = "tags"
    const val REGISTRATION = "registration"

    const val PICTURE_URL = "pictureUrl"

    @Deprecated("use PICTURE_URL instead")
    const val PICTUREURL = PICTURE_URL
    const val PICTURE_ALT_TEXT = "pictureAltText"
    const val PICTURE_COPYRIGHT = "pictureCopyright"

    const val COLLECTORNAME = "collectorName"
    const val SOURCES = "sources"
    const val ADDITIONAL_EVENTS_URL = "additionalEventsFromSourceUrl"

    // location properties
    const val LOCATION_NAME = "location.name"
    const val LOCATION_URL = "location.url"
    const val LOCATION_COORDINATES = "location.coordinates"
    const val LOCATION_CITY = "location.city"
    const val LOCATION_ADDRESS = "location.address"

    // accessibility properties
    const val ACCESSIBILITY_ACCESSIBLEENTRY = "accessibility.accessibleEntry"
    const val ACCESSIBILITY_ACCESSIBLESEATS = "accessibility.accessibleSeats"
    const val ACCESSIBILITY_ACCESSIBLETOILETS = "accessibility.accessibleToilets"
    const val ACCESSIBILITY_AKTIVPASSLINZ = "accessibility.accessibleAktivpassLinz"
    const val ACCESSIBILITY_KULTURPASS = "accessibility.accessibleKulturpass"

    // concert properties
    const val CONCERT_GENRE = "concert.genre"
    const val CONCERT_BANDLIST = "concert.bandlist"

    object Image {
        const val URL = "url"
        const val ALT_TEXT = "altText"
        const val COPYRIGHT = "copyright"
    }
}
