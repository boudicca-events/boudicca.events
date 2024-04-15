# Search Service

The Search Service offers an API for searching all Entries.

It is a simple SpringBoot app you can find [here](../../boudicca.base/search)

## Search API

This API offers endpoints for building search functionality, it is implemented as a Spring RestController you can find it [here](../../boudicca.base/search/src/main/kotlin/base/boudicca/search/controller/SearchController.kt)

* `/queryEntries`: Endpoint for querying data. You give it the actual search query (see [QUERY.md](../QUERY.md)), the offset and the size of the Entries you want. These two last parameters make it possible to implement paging/show more functionality
* `/filtersFor`: This endpoint allows you to get all distinct values for certain entry fields. You can use them to offer select-boxes in your search interface. Please note that you have to choose your fields carefully here, you could end up with waaay too many distinct values otherwise.

## SynchronizationService

This service is responsible for updating the data every hour. It basically is a scheduled job which runs every hour, fetches the data from the configured EventDb and then sends an `EntriesUpdatedEvent` with the new data. The other services receive this event and update their internal state to use that from now on.

Data in the Search Service is normally cached and only updated every hour, so for local development there is the `boudicca.localMode` property, which will actively update the data everytime an endpoint is called.

## FiltersService

Service which handles the `/filtersFor` endpoint-

## QueryService

The query service handles the evaluation of the actual search queries. For more information about the Query itself see [QUERY.md](../QUERY.md)

This consists of following steps:

1. Parsing the Query with the [QueryParser](../../boudicca.base/search/src/main/kotlin/base/boudicca/search/service/query/QueryParser.kt) which results in an [Expression](../../boudicca.base/search/src/main/kotlin/base/boudicca/search/service/query/Expression.kt) object. This is an AST representation of the parsed Query.
2. Using an [Evaluator](../../boudicca.base/query-lib/src/main/kotlin/base/boudicca/query/evaluator/Evaluator.kt) to run the AST expression from step 1.
3. Return the correct page of the data, specified by the offset and size parameters of the REST endpoint

