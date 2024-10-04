# Using Boudiccas REST Apis

There are multiple ways to use our REST Apis.

1. With our published client (recommended)
2. By generating your own client via OpenAPI
3. By just calling Boudicca

## Boudicca Clients

We publish client libraries which are easy to use Java/Kotlin based classes which allow you to connect to any deployment
of Boudicca. You will need Java 21 though.

To use them just add a dependency like

```
Gradle:
implementation("events.boudicca:search-client:0.5.0")
```

or

```
Maven:
<dependency>
    <groupId>events.boudicca</groupId>
    <artifactId>search-client</artifactId>
    <version>0.5.0</version>
</dependency>
```

and instantiate and use a Client like this:

```
SearchClient("https://search.boudicca.events")
    .queryEvents(QueryDTO(""" "location.name" equals "posthof" """, 0, 30))
    .result.forEach { println(it) }
```

Currently we have following clients (the groups is always `events.boudicca`):

* EventDB Publisher-side: `publisher-client`
* EventDB Ingest-side: `ingest-client`
* Search Service: `search-client`
* Enricher: `enricher-client`

For more examples how to use these clients please look at the sample repo: https://github.com/boudicca-events/client-samples

## Generate your own client with OpenApi

Maybe our clients do not work for you, for example if you use another language like Javascript/Typescript.
In those cases we do also publish our openapi.json-spec files to maven central, which means you can use them to create
your own client!

You can access and download them directly from maven central via those links:

* [EventDB](https://repo1.maven.org/maven2/events/boudicca/eventdb-api/0.2.0/eventdb-api-0.2.0-openapi.json)
* [Search Service](https://repo1.maven.org/maven2/events/boudicca/search-api/0.2.0/search-api-0.2.0-openapi.json)
* [Enricher](https://repo1.maven.org/maven2/events/boudicca/enricher-api/0.2.0/enricher-api-0.2.0-openapi.json)

Or you can use them as a dependency with classifier `openapi` and (file-)type `json` like

```
Gradle 
openapiSpec("events.boudicca:search-api:0.5.0:openapi@json")
```

For a full example how to generate your own client with gradle and those dependencies please have a look at our sample: https://github.com/boudicca-events/openapi-generate-sample

## Just calling Boudicca

You are of course open to just use your own http client and call our REST Apis.
We support you here by having SwaggerUI deployed with all our applications at the path `<base>/swagger-ui/index.html`
where you can just play around and test our REST Apis

Currently, we have three services:

* EventDB: https://eventdb.boudicca.events/swagger-ui/index.html
* Search: https://search.boudicca.events/swagger-ui/index.html
* Enricher: https://enricher.boudicca.events/swagger-ui/index.html