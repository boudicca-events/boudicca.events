include("boudicca.base")
include("boudicca.events")

include("boudicca.base:boudicca-api")
include("boudicca.base:enricher")
include("boudicca.base:enricher-client")
include("boudicca.base:enricher-openapi")
include("boudicca.base:enricher-utils")
include("boudicca.base:ingest-client")
include("boudicca.base:eventcollector-client")
include("boudicca.base:eventdb")
include("boudicca.base:eventdb-openapi")
include("boudicca.base:publisher-client")
include("boudicca.base:publisher-event-ical")
include("boudicca.base:publisher-event-html")
include("boudicca.base:search")
include("boudicca.base:search-openapi")
include("boudicca.base:search-client")
include("boudicca.base:semantic-conventions")

include("boudicca.events:eventcollectors")

rootProject.name = "boudicca"