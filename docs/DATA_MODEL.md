# Boudicca's Data Model

## Overview

Boudicca's most basic data structure is an Entry, which is a collection of Key->Value Pairs, which are called Properties.
Those keys and values are both UTF-8 encoded strings and represent information about the entry.
For example `name`->`My Entry` would be a Property with key "name" and value "My Entry" and could represent the name of this Entry.

Sometimes you need more information about the value of one property, for example which language the value is in, or if this value is supposed to be a number, text, date, ...
Boudicca encodes this information into the Key of the Property by appending "VariantInformation" to the base key value, which looks like `:VariantName=VariantValue`
For example to specify that this name is in english we could have the Property `name:lang=en`->`My Entry` or to specify that something is a number we can write `price:format=number`->`20`.
TODO add links to variant sections?
You can of course chain and combine those VariantInformations, but be aware that most of the time the same VariantInformation is only allowed to appear once per Property.

An Entry is a very generic way of viewing the data in Boudicca and most of the time you will only think about and work with more specialized kinds of Entries. 
For Boudicca the most important one is the "Event", which represents a generic event. To make those events interoperable we have standardised properties and their meaning which you can find in our [Semantic Conventions](docs/SEMANTIC_CONVENTIONS.md) documentation.
But nothing is stopping you from adding and using your own properties to Entries or Events.

There are many ways to serialize Boudicca-Entries, but the standard way is to serialize them into UTF-8 encoded JSON. 
Here one Entry will be mapped to a JSON-Object where the properties are JSON-Object key-value pairs and a list of Entries will be a JSON-Array.

## Definitions

At this point we need to specify terminology a bit better:

* Entry: One generic entry in our database, which consists of multiple "Properties"
* Property: "Key"->"Value" pair, where key and value are UTF-8 encoded strings.
* Event: A specialised "Entry", which represents an event and has two mandatory keys: "name" and "startDate". See [Semantic Conventions](docs/SEMANTIC_CONVENTIONS.md) for more information.
* Key: Key value of one "Key"->"Value" mapping of one entry. Can contain multiple variant information. Format is "PropertyName"\{:"VariantInformation"}
* Value: The value of one property. Has to conform to the specified variant data, so conform to the correct language and/or format.
* PropertyName: Name of the property of an entry. Can only consist of letters, numbers and "." TODO is that ok? do we want to allow other special chars?
* VariantInformation: Specifying what kind of variant this key is. Format is "VariantName"="VariantValue"
* VariantName: The name of the variant. TODO what to do if unknown value? ignore the value? but if it is the only one?
* VariantValue: The value of the variant. For example, for the variant Format it should be the format the value is in. For Language it should be the language of the Value.

Currently supported variants are:
* Language: which language a value is in
* Format: what kind of data this value is, for example text, dates, ...

If a certain variant makes sense for a certain property needs to be decided per property basis.

## Language Variant

Has the VariantName "lang" and the value is a two-letter language abbreviation, like "en", "de", "fr", ...

TODO define what that means for searching, for displaying, how languages are chosen and so on

## Format Variant

Has the VariantName "format" and specifies what kind of data and in which format the Value is.

Boudicca currently supports those formats:
* Text (VariantValue "text" as well as the default when no format is given)
* Numbers (VariantValue "number")
* Dates (VariantValue "date")
* Lists (VariantValue "list")
* Markdown (VariantValue "markdown")

TODO specify if you are allowed to add new formats yourself and what this means for working with Boudicca software.

TODO make below sections more standardized with information. like what about sorting, searching, ...

### Text

Simple text

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

The format is the "Date and time with the offset" format of the [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) standard. TODO not sure if we really want to accept all ISO formats, i guess there are local ones in there as well (ones without timezone information)

Examples:
* 2024-04-27T23:59:00+02:00
* 2024-04-27T11:00:00Z

### Lists

Knowledge about lists is mostly important for contains searches and search faceting. We cannot sort lists.

The format is a comma "," seperated list of values. That means that commas occurring in a value have to be escaped by a backslash "\" and occurring backslashes also have to be escaped by a backslash.

Examples:
* value1 (this is a list consisting only of one value "value1")
* value1, value2 (this is a list consisting of two values "value1" and "value2")
* val\\\\ue1,val\\\,ue2 (this is a list consisting of two values with escaped characters in them "val\\ue1" and "val,ue2") (this example is wrong when looked at directly, please only look at it rendered through an markdown engine, because there are too many "\\" in the unrendered text)
* value1,,value2 (this is a list consisting of three values "value1", "" (an empty value) and "value2")
* value1, value2 (this is a list consisting of two values "value1" and " value2" (please note the preceding whitespace in the second value))
* (the empty string -> a list with a single empty value)
* , (this is a list consisting of two empty values, please note that there is no way to specify an empty list, you need to remove the property for this)

### Markdown

Knowledge about markdown is important for rendering it correctly.

We agree to use standard markdown according to [Markdown](https://en.wikipedia.org/wiki/Markdown).

## Canonical Form

If you have more than one VariantInformation in one key there are multiple ways to write this by changing the order of the VariantInformations. For example:

`mykey:foo=bar:fuzz=bizz`->`my value` and `mykey:fuzz=bizz:foo=bar`->`my value`

To make working with keys and VariantInformation in a map easier the only correct way to write a key is the canonical way:
VariantInformation should be alphabetically ordered by their VariantName, and then by their VariantValue.
So for the example above, the canonical way is `mykey:foo=bar:fuzz=bizz`->`my value` because "foo" is alphabetically ordered before "fuzz".

Another example for repeated VariantNames is `mykey:foo=bar:foo=bizz`->`my value` which is ordered correctly because while the VariantName "foo" is the same the values "bar" and "bizz" are different and "bar" has to come before "bizz".

If you are using Boudicca's libraries to work with variants the library will make sure all keys are canonical, and our EventDB will also make sure that all ingested keys are changed to be canonical. That also means that all keys you get from our EventDB and our SearchService are guaranteed to be canonical.