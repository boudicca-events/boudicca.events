package base.boudicca

import base.boudicca.model.EventCategory
import base.boudicca.model.RecurrenceType
import base.boudicca.model.Registration
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * constants for all our known properties and their corresponding [Property]
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Suppress("unused") // Definitions are maintained for API completeness and may currently be unused
object SemanticKeys {

    // general properties

    const val NAME = "name"
    val NAME_PROPERTY = TextProperty(NAME)

    const val STARTDATE = "startDate"
    val STARTDATE_PROPERTY = DateProperty(STARTDATE)

    const val ENDDATE = "endDate"
    val ENDDATE_PROPERTY = DateProperty(ENDDATE)

    const val URL = "url"
    val URL_PROPERTY = UriProperty(URL)

    const val TYPE = "type"
    val TYPE_PROPERTY = TextProperty(TYPE)

    const val CATEGORY = "category"
    val CATEGORY_PROPERTY = EnumProperty(CATEGORY) { value -> EventCategory.valueOf(value) }

    const val DESCRIPTION = "description"
    val DESCRIPTION_TEXT_PROPERTY = TextProperty(DESCRIPTION)
    val DESCRIPTION_MARKDOWN_PROPERTY = MarkdownProperty(DESCRIPTION)

    const val RECURRENCE_TYPE = "recurrence.type"
    val RECURRENCE_TYPE_PROPERTY = EnumProperty(RECURRENCE_TYPE) { value -> RecurrenceType.valueOf(value) }

    const val RECURRENCE_INTERVAL = "recurrence.interval"
    val RECURRENCE_INTERVAL_PROPERTY = TextProperty(RECURRENCE_INTERVAL)

    const val TAGS = "tags"
    val TAGS_PROPERTY = ListProperty(TAGS)

    const val REGISTRATION = "registration"
    val REGISTRATION_PROPERTY = EnumProperty(REGISTRATION) { value -> Registration.valueOf(value) }

    const val PICTURE_URL = "pictureUrl"
    val PICTURE_URL_PROPERTY = UriProperty(PICTURE_URL)

    const val PICTURE_ALT_TEXT = "pictureAltText"
    val PICTURE_ALT_TEXT_PROPERTY = TextProperty(PICTURE_ALT_TEXT)

    const val PICTURE_COPYRIGHT = "pictureCopyright"
    val PICTURE_COPYRIGHT_PROPERTY = TextProperty(PICTURE_COPYRIGHT)

    const val COLLECTORNAME = "collectorName"
    val COLLECTORNAME_PROPERTY = TextProperty(COLLECTORNAME)

    const val SOURCES = "sources"
    val SOURCES_PROPERTY = ListProperty(SOURCES)

    const val ADDITIONAL_EVENTS_URL = "additionalEventsFromSourceUrl"
    val ADDITIONAL_EVENTS_URL_PROPERTY = UriProperty(ADDITIONAL_EVENTS_URL)

    // location properties

    const val LOCATION_NAME = "location.name"
    val LOCATION_NAME_PROPERTY = TextProperty(LOCATION_NAME)

    const val LOCATION_URL = "location.url"
    val LOCATION_URL_PROPERTY = UriProperty(LOCATION_URL)

    const val LOCATION_COORDINATES_LAT = "location.coordinates.lat"
    val LOCATION_COORDINATES_LAT_PROPERTY = NumberProperty(LOCATION_COORDINATES_LAT)

    const val LOCATION_COORDINATES_LON = "location.coordinates.lon"
    val LOCATION_COORDINATES_LON_PROPERTY = NumberProperty(LOCATION_COORDINATES_LON)

    const val LOCATION_CITY = "location.city"
    val LOCATION_CITY_PROPERTY = TextProperty(LOCATION_CITY)

    const val LOCATION_ADDRESS = "location.address"
    val LOCATION_ADDRESS_PROPERTY = TextProperty(LOCATION_ADDRESS)

    //TODO add those into semantic conventions
    const val LOCATION_OSM_ID = "location.osmId"
    val LOCATION_OSM_ID_PROPERTY = TextProperty(LOCATION_OSM_ID)

    const val LOCATION_DESCRIPTION = "location.description"
    val LOCATION_DESCRIPTION_PROPERTY = TextProperty(LOCATION_DESCRIPTION)

    const val LOCATION_CONTACT_PHONE = "location.contact.phone"
    val LOCATION_CONTACT_PHONE_PROPERTY = TextProperty(LOCATION_CONTACT_PHONE)

    const val LOCATION_CONTACT_EMAIL = "location.contact.email"
    val LOCATION_CONTACT_EMAIL_PROPERTY = TextProperty(LOCATION_CONTACT_EMAIL)

    const val LOCATION_WIKIPEDIA = "location.wikipedia"
    val LOCATION_WIKIPEDIA_PROPERTY = TextProperty(LOCATION_WIKIPEDIA)

    const val LOCATION_WIKIDATA = "location.wikidata"
    val LOCATION_WIKIDATA_PROPERTY = TextProperty(LOCATION_WIKIDATA)

    // accessibility properties

    const val ACCESSIBILITY_ACCESSIBLEENTRY = "accessibility.accessibleEntry"
    val ACCESSIBILITY_ACCESSIBLEENTRY_PROPERTY = BooleanProperty(ACCESSIBILITY_ACCESSIBLEENTRY)

    const val ACCESSIBILITY_ACCESSIBLESEATS = "accessibility.accessibleSeats"
    val ACCESSIBILITY_ACCESSIBLESEATS_PROPERTY = BooleanProperty(ACCESSIBILITY_ACCESSIBLESEATS)

    const val ACCESSIBILITY_ACCESSIBLETOILETS = "accessibility.accessibleToilets"
    val ACCESSIBILITY_ACCESSIBLETOILETS_PROPERTY = BooleanProperty(ACCESSIBILITY_ACCESSIBLETOILETS)

    const val ACCESSIBILITY_AKTIVPASSLINZ = "accessibility.accessibleAktivpassLinz"
    val ACCESSIBILITY_AKTIVPASSLINZ_PROPERTY = BooleanProperty(ACCESSIBILITY_AKTIVPASSLINZ)

    const val ACCESSIBILITY_KULTURPASS = "accessibility.accessibleKulturpass"
    val ACCESSIBILITY_KULTURPASS_PROPERTY = BooleanProperty(ACCESSIBILITY_KULTURPASS)

    // concert properties

    const val CONCERT_GENRE = "concert.genre"
    val CONCERT_GENRE_PROPERTY = TextProperty(CONCERT_GENRE)

    const val CONCERT_BANDLIST = "concert.bandlist"
    val CONCERT_BANDLIST_PROPERTY = ListProperty(CONCERT_BANDLIST)


}
