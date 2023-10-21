include("boudicca.base")
include("boudicca.events")

include("boudicca.base:enricher")
include("boudicca.base:enricher-openapi")
include("boudicca.base:enricher-utils")
include("boudicca.base:ingest-api")
include("boudicca.base:eventcollector-api")
include("boudicca.base:eventdb")
include("boudicca.base:eventdb-openapi")
include("boudicca.base:publisher-api")
include("boudicca.base:publisher-event-ical")
include("boudicca.base:publisher-event-html")
include("boudicca.base:search")
include("boudicca.base:search-openapi")
include("boudicca.base:search-api")
include("boudicca.base:semantic-conventions")

include("boudicca.events:eventcollectors")

rootProject.name = "boudicca"