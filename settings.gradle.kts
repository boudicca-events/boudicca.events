plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
include("boudicca.base")
include("boudicca.events")

include("boudicca.base:enricher")
include("boudicca.base:enricher-api")
include("boudicca.base:enricher-client")
include("boudicca.base:enricher-openapi")
include("boudicca.base:enricher-utils")
include("boudicca.base:ingest-client")
include("boudicca.base:eventcollector-client")
include("boudicca.base:eventdb")
include("boudicca.base:eventdb-api")
include("boudicca.base:eventdb-openapi")
include("boudicca.base:publisher-client")
include("boudicca.base:publisher-event-ical")
include("boudicca.base:publisher-event-html")
include("boudicca.base:search")
include("boudicca.base:search-openapi")
include("boudicca.base:search-api")
include("boudicca.base:search-client")
include("boudicca.base:semantic-conventions")
include("boudicca.base:query-lib")
include("boudicca.base:remote-collector")
include("boudicca.base:remote-collector:remote-collector-api")
include("boudicca.base:remote-collector:remote-collector-openapi")
include("boudicca.base:remote-collector:remote-collector-client")
include("boudicca.base:remote-collector")

include("boudicca.events:eventcollectors")
include("boudicca.events:publisher-event-html")

rootProject.name = "boudicca"
include("boudicca.base:publisher-event-html-a11ytests")
findProject(":boudicca.base:publisher-event-html-a11ytests")?.name = "publisher-event-html-a11ytests"
