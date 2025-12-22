# Search Service

The Search Service offers an API for searching all Entries.

It is a simple SpringBoot app you can find [here](../../boudicca.base/search)

## Search API

This API offers endpoints for building search functionality, it is implemented as a Spring RestController you can find
it [here](../../boudicca.base/search/src/main/kotlin/base/boudicca/search/controller/SearchController.kt)

* `/queryEntries`: Endpoint for querying data. You give it the actual search query (see [QUERY.md](../QUERY.md)), the offset and the size of the Entries you want. These two last
  parameters make it possible to implement paging (aka the show more functionality)
* `/filtersFor`: This endpoint allows you to get all distinct values for certain entry properties. You can use them to offer select-boxes in your search interface. Please note that
  you have to choose your properties carefully here, you could end up with waaay too many distinct values otherwise.

## SynchronizationService

This service is responsible for updating the data every hour. It basically is a scheduled job which runs every hour, fetches the data from the configured EventDb and then sends an
`EntriesUpdatedEvent` with the new data. The other services receive this event and update their internal state to use that from now on.

Data in the Search Service is normally cached and only updated every hour, so for local development there is the `boudicca.devMode` property, which will actively update the data
everytime an endpoint is called.
This property is already set in the `LocalSearch` launch config.

## FiltersService

Service which handles the `/filtersFor` endpoint.

## QueryService

The query service handles the evaluation of the actual search queries. For more information about the Query itself see [QUERY.md](../QUERY.md)

This consists of following steps:

1. Parsing the Query with the [QueryParser](../../boudicca.base/search/src/main/kotlin/base/boudicca/search/service/query/QueryParser.kt) which results in
   an [Expression](../../boudicca.base/search/src/main/kotlin/base/boudicca/search/service/query/Expression.kt) object. This is an AST representation of the parsed Query.
2. Using an [Evaluator](../../boudicca.base/query-lib/src/main/kotlin/base/boudicca/query/evaluator/Evaluator.kt) to run the AST expression from step 1.
3. Return the correct page of the data, specified by the offset and size parameters of the REST endpoint

## Parsing and evaluating the Boudicca Query Language

Using queries is a three-step process: Lexing, Parsing, Evaluation

### Lexing

The Lexer is responsible for taking the query-string and breaking it up into a list of tokens. This means the handling
for escaping text and recognizing keywords happens here.

For example the query `"name" equals "BigBand Blues"` will result in the three
tokens: `text("name"), equals, text("BigBand Blues")`

### Parsing

The Parser gets a list of tokens from the lexer and parses them into some sort of AST (Abstract Syntax Tree), in our
case called `Expression`. These are a hierarchical representation of all the operations happening in this query.

For example the tokens: `not, text("name"), contains, text("BigBand")` will result in the
Expression `NOT(CONTAINS("name","BigBand"))` (this would be the output of the toString() method of the Expression you
get from the parser)

### Evaluation

An Evaluator takes an Expression from the Parser and uses it to filter all the events it knows and returns all
matching ones.

For example the SimpleEvaluator has a collection of Entries and just iterates over each Event and evaluates the Expression on it.
