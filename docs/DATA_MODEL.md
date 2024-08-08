# Boudicca's Data Model

## Overview

TODO i think this overview is too specific already


Boudicca's most basic data structure is an Entry, which is a collection of Key->Value Pairs, which are called Properties.
Those keys and values are both UTF-8 encoded strings and represent information about the entry.
For example `name`->`My Entry` would be a Property with key "name" and value "My Entry" and could represent the name of this Entry.

Sometimes you need more information about the value of one property, for example which language the value is in, or if this value is supposed to be a number, text, date, ...
Boudicca encodes this information into the Key of the Property by appending VariantInformation to the base key value, which looks like `:VariantName=VariantValue`
For example to specify that this name is in english we could have the Property `name:lang=en`->`My Entry` or to specify that something is a number we can write `price:format=number`->`20`.
See [Format](#format-variant) and [Language](#language-variant) for more information.
You can of course chain and combine those VariantInformations, but be aware that most of the time the same VariantInformation is only allowed to appear once per Property and watch out for the [canonical form](#canonical-form).

An Entry is a very generic way of viewing the data in Boudicca and most of the time you will only think about and work with more specialized kinds of Entries. 
For Boudicca the most important one is the "Event", which represents a generic event. To make those events interoperable we have standardised properties and their meaning which you can find in our [Semantic Conventions](docs/SEMANTIC_CONVENTIONS.md) documentation.
But nothing is stopping you from adding and using your own properties to Entries or Events.

There are many ways to serialize Boudicca-Entries, but the standard way is to serialize them into UTF-8 encoded JSON. 
Here one Entry will be mapped to a JSON-Object where the properties are JSON-Object key-value pairs and a list of Entries will be a JSON-Array.

## Definitions

At this point we need to specify terminology a bit better:

* Entry: One generic entry in our database, which consists of multiple "Properties"
* Event: A specialised "Entry", which represents an event and has two mandatory keys: "name" and "startDate". See [Semantic Conventions](docs/SEMANTIC_CONVENTIONS.md) for more information.
* Property: "Key"->"Value" pair, where key and value are UTF-8 encoded strings.
* Key: Key value of one "Key"->"Value" mapping of one entry. Can contain multiple variant information. Format is `PropertyName[:VariantInformation]*`
* Value: The value of one property. Has to conform to the specified variant data, so conform to the correct language and/or format.
* PropertyName: Name of the property of an entry. Can only consist of letters, numbers and "."
* VariantInformation: Specifying what kind of variant this key is. Format is `VariantName=VariantValue`
* VariantName: The name of the variant.
* VariantValue: The value of the variant. For example, for the variant Format it should be the format the value is in. For Language it should be the language of the Value.

Currently supported variants are:
* [Language](#language-variant): which language a value is in
* [Format](#format-variant): what kind of data this value is, for example text, dates, ...

If a certain variant makes sense for a certain property needs to be decided per property and that properties defined meaning. For example, it does not make sense to have a language variant for "startDate".


TODO maybe we can define generic selecting vs searching strategies? 

## Language Variant

Has the VariantName "lang" and the value is a two-letter language abbreviation, like "en", "de", "fr", ... 
Denotes the language the value is in. Without a language variant it is assumed that this is the "default" language and will be used as a fallback if the wanted language is not available. TODO does this seem right?

### Language in the HTML-Publisher

If the user has selected a language (feature pending :P ) the Publisher will preferably select the value with the correct language variant.
If this variant is not available (or the user has no language selected) it will fall back to the default language (aka without any lang variant).
If this is not available it will order all remaining variants alphabetically by key and select the first one.

### Language in the ical-Publisher

like html publisher? do we have a language here? 

### Language in Search Service

The Search Service will search all Variants matching the given key and more specific ones.
So for example, if you make a contains-query like `"name" contains "my event"` Search Service will search keys without variants like `name` and keys with variants like `name:lang=de`.
But if you search for `"name:lang=de" contains "my event"` then Search Service will NOT search keys without variants like `name`. TODO this seems wrong? maybe we should make it fall back to the default language as well, but not other variants.

TODO what about wildcard searches?

TODO refine above sections

## Format Variant

Has the VariantName "format" and specifies what kind of data and in which format the Value is.

Boudicca currently supports those formats:
* Text (VariantValue "text" as well as the default when no format is given)
* Numbers (VariantValue "number")
* Dates (VariantValue "date")
* Lists (VariantValue "list")
* Markdown (VariantValue "markdown")


TODO make below sections more standardized with information. like what about sorting, searching, ...

### Text

Format: Arbitrary text, no restrictions

Sorting: Alphabetically, case-insensitive (so `aAbB` instead of `abAB`)

Searching: Works with Equals and Contains queries.   

### Numbers


Format: Allowed are integers, negative numbers and floating point numbers  

Examples:
* 0
* 1
* -5
* 0.5
* ...?

Sorting: By numerical value

Searching: Works with Equals queries. Numbers need to be equal. TODO we probably need greater and smaller-queries here.

### Dates

Format: The format is the "Date and time with the offset" format of the [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) standard. TODO not sure if we really want to accept all ISO formats, i guess there are local ones in there as well (ones without timezone information)

Examples:
* 2024-04-27T23:59:00+02:00
* 2024-04-27T11:00:00Z

Sorting: By time.

Searching: Works with Equals, After, Before and Duration queries.

### Lists

Format: The format is a comma "," seperated list of values. That means that commas occurring in a value have to be escaped by a backslash "\" and occurring backslashes also have to be escaped by a backslash.

Examples:
* value1 (this is a list consisting only of one value "value1")
* value1, value2 (this is a list consisting of two values "value1" and "value2")
* val\\\\ue1,val\\\,ue2 (this is a list consisting of two values with escaped characters in them "val\\ue1" and "val,ue2") (this example is wrong when looked at directly, please only look at it rendered through an markdown engine, because there are too many "\\" in the unrendered text)
* value1,,value2 (this is a list consisting of three values "value1", "" (an empty value) and "value2")
* value1, value2 (this is a list consisting of two values "value1" and " value2" (please note the preceding whitespace in the second value))
* (the empty string -> a list with a single empty value)
* , (this is a list consisting of two empty values, please note that there is no way to specify an empty list, you need to remove the property for this)

Examples:
* 2024-04-27T23:59:00+02:00
* 2024-04-27T11:00:00Z

Sorting: Alphabetically, case-insensitive (so `aAbB` instead of `abAB`). WARNING: This is like text and is most likely not what you want, do not rely on sorting lists.

Searching: Works with Equals and Contains queries. 


### Markdown

Format: We agree to use standard markdown according to [Markdown](https://en.wikipedia.org/wiki/Markdown)

Sorting: Alphabetically, case-insensitive (so `aAbB` instead of `abAB`)

Searching: Works with Equals and Contains queries

Knowledge about markdown is important for rendering it correctly, otherwise it is treated like text.


### Format in the HTML-Publisher

For displaying properties the HTML-Publisher follows the [Semantic Conventions](docs/SEMANTIC_CONVENTIONS.md) and tries to find the variant with the highest priority.
For searching, the filters will be kept generic, so no variant will be added to the search term. TODO is this a good idea?

### Format in the ical-Publisher

like html publisher?

### Format in Search Service

The Search Service will search all Variants matching the given key and more specific ones.
So for example, if you make a contains-query like `"name" contains "my event"` Search Service will search keys without variants like `name` and keys with variants like `name:format=list`.
But if you search for `"name:format=list" contains "my event"` then Search Service will NOT search keys without variants like `name`. TODO this seems wrong? maybe we should make it fall back to the default language as well, but not other variants.
TODO seems awfully similar to language, should be a pattern here?
TODO what about wildcard searches?

TODO refine above sections

## Canonical Form

If you have more than one VariantInformation in one key there are multiple ways to write this by changing the order of the VariantInformations. For example:

`mykey:foo=bar:fuzz=bizz`->`my value` and `mykey:fuzz=bizz:foo=bar`->`my value`

To make working with keys and VariantInformation in a map easier the only correct way to write a key is the canonical way:
VariantInformation should be alphabetically ordered by their VariantName, and then by their VariantValue.
So for the example above, the canonical way is `mykey:foo=bar:fuzz=bizz`->`my value` because "foo" is alphabetically ordered before "fuzz".

Another example for repeated VariantNames is `mykey:foo=bar:foo=bizz`->`my value` which is ordered correctly because while the VariantName "foo" is the same the values "bar" and "bizz" are different and "bar" has to come before "bizz".

If you are using Boudicca's libraries to work with variants the library will make sure all keys are canonical, and our EventDB will also make sure that all ingested keys are changed to be canonical. That also means that all keys you get from our EventDB and our SearchService are guaranteed to be canonical.

## Unknown Variants

Boudicca will ignore all Variants it does not understand and treat the keys as if those Variants are not here. This behaviour is suggested for all Software handling Boudicca Keys.