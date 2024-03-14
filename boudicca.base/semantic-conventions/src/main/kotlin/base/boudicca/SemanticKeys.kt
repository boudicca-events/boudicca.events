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
    const val DESCRIPTION_MARKDOWN = "description.markdown"
    const val RECURRENCE_TYPE = "recurrence.type"
    const val RECURRENCE_INTERVAL = "recurrence.interval"
    const val TAGS = "tags"
    const val REGISTRATION = "registration"
    @Deprecated("use pictures.json instead")
    const val PICTUREURL = "pictureUrl"
    const val PICTURES_JSON = "pictures.json"
    const val COLLECTORNAME = "collectorName"
    const val SOURCES = "sources"
    const val SOURCE_EVENT_PAGE = "source.event.page"
    const val SOURCE_EVENT_LIST = "source.event.list"
    const val SOURCE_EVENT_DETAILS = "source.event.details"

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
