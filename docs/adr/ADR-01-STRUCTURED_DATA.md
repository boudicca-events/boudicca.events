# ADR 01 Structured Data

## Why?

Our data-model was and in some way still is a simple String -> String key to value map, 
but some features require us to have some knowledge about the value, so it can be handled correctly.
For example to sort or compare dates, we have to know how to parse this string as a date, so this date has to follow a certain format.
Other examples would be sorting numbers, or having a list of values for a certain property.

Another, related, topic we want to support is having different variants for a key, the most important feature for that is translations for different languages.

Now there are two decision we have to make: 
1) How to transport the format and/or variant for the value of a specific key
2) What kind of formats/structure we want to support

## How to transport variant information

Certain parts of Boudicca need to know about the format of the value, for example the search service for searching, or publisher for rendering a value correctly.

### Possible Solutions

#### Predetermine formats for keys

Also called having a schema, basically means we have a pre-defined format for every key/event property.

Pros:
* We do not have to transport any information otherwise
* Easy to implement and maintain

Cons:
* Not flexible, users cannot introduce their own properties with other formats then the default string

#### Guess formats for keys

We could make guesses based on the format of the key/value and/or the query operator used to search.

Pros:
* We do not have to transport any information otherwise
* Users can introduce their own properties with formats

Cons:
* Errorprone, we cannot guarantee correct handling

#### Encode the format into the value

Put the format information somewhere in the value, for example as a prefix

Pros:
* Users can introduce their own properties with formats

Cons:
* Even simple strings have to have a format prefix, otherwise we cannot guarantee correct handling
* Unclear how to make variants of the same value, since the key would be the same

#### Encode the format into the key

Put the format information somewhere in the key, for example as a suffix

Pros:
* Users can introduce their own properties with formats
* Easy to have multiple variants, since the key is different

Cons:
* Makes key-handling more complex, since there are now multiple keys for one "base-key"


### Decision

We decided on putting the format and variant information into the key, as different key=value pairs seperated by ":"

At this point we need to introduce some more precise terminology:

TODO move or copy this glossary somewhere else?
* Entry: One generic entry in our database, which consists of multiple "Key"->"Value" mappings, where key and value are strings.
* Event: A specialised "Entry", which represents an event and has two mandatory keys: "name" and "startDate"
* Key: Key value of one "Key"->"Value" mapping of one entry. Can contain multiple variant information. Format is "PropertyName"\{:"VariantInformation"}
* PropertyName: Name of the property of an entry. Can only consist of letters, numbers and "." TODO is that ok? do we want to allow other special chars?
* VariantInformation: Specifying what kind of variant this key is. Format is "VariantName"="VariantValue"

Currently supported variants are:
* Language: which language a value is in
* Format: what kind of format the value is in

If a certain variant makes sense for a certain property needs to be decided per property basis.

#### Language Variant

Has the VariantName "lang" and the value is a two-letter language abbreviation, like "en", "de", "fr", ...

TODO define what that means for searching, for displaying, how languages are chosen and so on

#### Format Variant

Has the VariantName "format" and for the value see the [Formats](#formats) section.

TODO define more stuff here?


## Formats

We decided to support following formats for now:
* Numbers (VariantValue "number")
* Dates (VariantValue "date")
* Lists (VariantValue "list")
* Markdown (VariantValue "markdown")

### Numbers

Knowledge about numbers is mostly important to correctly sort them.

The format looks like: TODO define format and possible numbers here

Examples:
* 0
* 1
* -5
* ...?

### Dates

Knowledge about dates is important for before/after + duration searches and to sort them correctly.

The format is the "Date and time with the offset" format of the [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) standard.

Examples:
* 2024-04-27T23:59:00+02:00
* 2024-04-27T11:00:00Z

### Lists

Knowledge about lists is mostly important for contains searches and search faceting. We cannot sort lists. (TODO can/should we?)

The format is a comma "," seperated list of values. That means that commas occurring in a value have to be escaped by a backslash "\" and occurring backslashes also have to be escaped by a backslash.

Examples:
* value1 (this is a list consisting only of one value "value1")
* value1, value2 (this is a list consisting of two values "value1" and "value2")
* val\\\\ue1,val\\\,ue2 (this is a list consisting of two values with escaped characters in them "val\\ue1" and "val,ue2") (this example is wrong when looked at directly, please only look at it rendered through an markdown engine, because there are too many "\\" in the unrendered text)
* (an empty list)
* value1,,value2 (this is a list consisting of three values "value1", "" (an empty value) and "value2")
* value1, value2 (this is a list consisting of two values "value1" and " value2" (please note the preceding whitespace in the second value))
* , (this is a list consisting of two empty values, please note that there is no way to specify a list with one empty value)

### Markdown

Knowledge about markdown is important for rendering it correctly.

The format is normal [Markdown](https://en.wikipedia.org/wiki/Markdown).