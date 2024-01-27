plugins {
    id("boudicca-openapi-generate-spec")
}

generateSpec {
    title = "Enricher API"
    description = "Enriches events with additional data, like Accessibility Information, Geolocation data, ..."
}