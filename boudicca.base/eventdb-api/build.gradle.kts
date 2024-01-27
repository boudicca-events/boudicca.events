plugins {
    id("boudicca-openapi-generate-spec")
}

generateSpec {
    title = "EventDB API"
    description = "The EventDB is responsible for ingesting, saving and then provide event data"
}