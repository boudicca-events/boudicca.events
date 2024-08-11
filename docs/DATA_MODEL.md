# Boudicca's Data Model

## Overview

Boudicca is a database containing Entries, which are a collection of `Key`->`Value` Pairs, so-called Properties.
For example `name`->`My Entry` would be a Property with key "name" and value "My Entry" and could represent the name of
this Entry.

An `Entry` is just a generic way of talking about the data in Boudicca, because it has no semantic meaning.
Normally you will work with `Events`, which are Entries representing some event.
To make those events interoperable we have standardised properties and their meaning which you can find in
our [Semantic Conventions](SEMANTIC_CONVENTIONS.md) documentation.
But nothing is stopping you from adding your own properties to Entries or Events.

Sometimes you need more information about the value of one property, for example which language the value is in, or if
this value is supposed to be a number, text, date, ...
Boudicca encodes this information into the Key of the Property by appending `Variants` to the property name, which look
like `:VariantName=VariantValue`
For example to specify that th property "name" is in english we could have the Property `name:lang=en`->`My Entry` or to specify
that something is a number we can write `price:format=number`->`20`.
See [Format](#format-variant) and [Language](#language-variant) for more information on those Variants.
You can of course chain and combine those Variants, but be aware that most of the time the same Variant is only allowed
to appear once per Key and watch out for the [canonical form](#canonical-form).


## Definitions

At this point we need to specify some terminology a bit better. Also have a look at the [Examples](#examples) below to
help you understand those definitions better.

* Entry: One generic entry in our database, which consists of multiple `Key`->`Value` pairs
* Event: A specialised `Entry`, which represents an event and has two mandatory keys: `name` and `startDate`.
  See [Semantic Conventions](SEMANTIC_CONVENTIONS.md) for more information.
* Property: All `Key`->`Value` pairs where the key has the same `PropertyName` but different `Variants`.
* PropertyVariant: One `Key`->`Value` pair of a property.
* Key: Key value of one `Key`->`Value` mapping. Can contain multiple `Variants`. Format is `PropertyName[:Variant]*` and
  it is encoded as a UTF-8 string.
* Value: The value of one `PropertyVariant`. Has to conform to the specified `Variant`, so conform to the correct
  language and/or format. It is encoded as a UTF-8 string.
* PropertyName: Name of one `Property` of an `Entry`. Can only consist of letters, numbers and `.`
* Variant: Specifying what kind of Variant (for example what language or what format) this key is. Format is `VariantName=VariantValue`
* VariantName: The name of the Variant.
* VariantValue: The value of the Variant. For example, for the Variant Format it should be the format the value is in.
  For Language, it should be the language of the Value.

Currently supported variants are:

* [Language](#language-variant): which language a value is in
* [Format](#format-variant): what kind of data this value is, for example text, dates, ...

If a certain Variant makes sense for a certain property needs to be decided per property and that properties defined
meaning. For example, it does not make sense to have a language variant for `startDate`.

### Examples

Let's look at an Event with following `Key`->`Value` pairs:

* `name` -> `My Event`
* `startDate:format=date` -> `2024-04-27T23:59:00+02:00`
* `description` -> `My Event is awesome!`
* `description:lang=de` -> `Mein Event ist großartig!`
* `description:format=markdown:lang=de` -> `#Mein Event ist großartig!`

The `Key`->`Value` pair `startDate:format=date` -> `2024-04-27T23:59:00+02:00` has the `Key` `startDate:format=date` and the `Value` `2024-04-27T23:59:00+02:00`

The `Key` `startDate:format=date` consists of the `PropertyName` `startDate` and one `Variant` `format=date`, seperated by `:`.

The `Variant` `format=date` consists of the `VariantName` `format` and the `VariantValue` `date`.

This Event has multiple `Properties`, whose `PropertyNames` are `name`, `startDate` and `description`.

While the properties `name` and `startDate` each only have one `PropertyVariant`, the property `description` has three `PropertyVariants `with Keys: `description`, `description:lang=de` and `description:format=markdown:lang=de`.

## Serialization

There are many ways to serialize Boudicca-Entries, but the standard way is to serialize them into UTF-8 encoded JSON.
Here one Entry will be mapped to a JSON-Object where the properties are JSON-Object key-value pairs and a list of
Entries will be a JSON-Array.

## Canonical Form

If you have more than one Variant in one key there are multiple ways to write this by changing the order of the
Variants. For example:

`mykey:foo=bar:fuzz=bizz`->`my value` and `mykey:fuzz=bizz:foo=bar`->`my value`

To make working with keys and Variants in a map easier the only correct way to write a key is the canonical way:
Variants should be alphabetically ordered by their VariantName, and then by their VariantValue.
So for the example above, the canonical way is `mykey:foo=bar:fuzz=bizz`->`my value` because "foo" is alphabetically
ordered before "fuzz".

Another example, for repeated VariantNames, is `mykey:foo=bar:foo=bizz`->`my value` which is ordered correctly because
while the VariantName "foo" is the same for both Variants, the values "bar" and "bizz" are different and "bar" has to
come before "bizz".

Empty VariantValues, like `lang=` mean that a Variant is NOT available, and is the same as it being not here. So in the
canonical form empty VariantValues are removed so that for example `description:lang=` will become `description`. Empty
VariantValues are allowed and used in [KeyFilters](#keyfilters).

Empty VariantNames on the other hand are invalid and will be rejected as an error. TODO do we?

If you are using Boudicca's libraries to work with Variants the library will make sure all keys are canonical, and our
EventDB will also make sure that all ingested keys are changed to be canonical. That also means that all keys you get
from our EventDB and our Search Service are guaranteed to be canonical.

## KeyFilters

When you are working with Properties which have multiple Variants or are using the [Boudicca Query Language](QUERY.md)
you often want to select some subset of all available PropertyVariants of one Property for querying or displaying.
To help you with this Boudicca offers support for so-called KeyFilters, which look like normal Keys.
One example would be `description:lang=de`, which is a filter filtering for the PropertyName `description` and the
Variant `lang=de`.
This is done by taking all available PropertyVariants for the Property `description` and then only taking those whose `lang`
Variant matches the value `de`. It does not matter if the PropertyVariant has additional Variants, those will be
ignored.

If KeyFilters return more than one PropertyVariant, the result will be sorted alphabetically by
their [Canonical Form](#canonical-form).

One difference compared to Keys is the empty VariantValue, like `lang=`. This means "select all PropertyVariants which
do NOT have the VariantName at all".
Another difference is that `*` is allowed as a PropertyName, this would match and select all Properties. You can still
add Variant Filters to the `*` filter.
You can also use `*` as a PropertyValue, like `lang=*`, which will select all PropertyVariants which do have a language Variant, regardless of what value this Variant has.

Let's look at some examples given following PropertyVariants of the `description` Property:

* `description`
* `description:lang=de`
* `description:lang=de:format=markdown`
* `description:lang=en`
* `description:lang=en:format=markdown`

With the filter `description:lang=de` you would get the two PropertyVariants `description:lang=de`
and `description:lang=de:format=markdown`.

With the filter `description` you would get all PropertyVariants as a result, because there is no filter except the
PropertyName.

With the empty VariantValue filter `description:lang=` you will only get `description` as a result, since this is the
only PropertyVariant without the `lang` Variant.

## Unknown Variants

Boudicca will ignore all Variants it does not understand and treat the keys as if those Variants are not here. This
behaviour is suggested for all Software handling Boudicca Keys.

## Variants and the Search Service

The Search Service will evaluate [Queries](QUERY.md) like the `contains` Query by searching all PropertyVariants
matching the [KeyFilter](#keyfilters) given to the Query and then performing the Query against all the PropertyVariants.
If any match, it will evaluate to true.
The only Variant the Search Service does know about is the [Format Variant](#format-variant), because this will
influence how searching, parsing and sorting works on those Variants.

If you create invalid combinations of Queries and Variants (like an `contains` Query and a `format=date` Variant) Search
Service will simply ignore those PropertyVariants as if they were not present.

So lets say you have the following PropertyVariants:

* `description`
* `description:lang=de`
* `description:lang=de:format=date`
* `description:lang=en`
* `description:lang=en:format=date`

For the query `"description" contains "my search text"` Search Service will first select all PropertyVariants which
match the given KeyFilter `description`, which are all of the above. Then it will discard all with an invalid format
Variant, which leaves us with the PropertyVariants `description`, `description:lang=de` and `description:lang=en`. It
will now evaluate the `contains` query against the value of those three PropertyVariants, and if any match the query
will evaluate to true.

## Variants and the HTML-Publisher

For displaying properties with different Formats the HTML-Publisher follows
the [Semantic Conventions](SEMANTIC_CONVENTIONS.md) and tries to find the Variant with the highest priority.

If the user has selected a language (feature pending :P ) the Publisher will preferably select a PropertyVariant with
the selected language.
If none are available (or the user has no language selected) it will fall back to the default language (aka
PropertyVariants without any language).
If none are available it will select any PropertyVariant with a valid Format.

For example, given following PropertyVariants:

* `description`
* `description:lang=de:format=date`
* `description:lang=en`
* `description:lang=en:format=number`

If the Publisher wants to show some property with the format `text` (aka no format Variant) and the user has selected
the language `de`, the Publisher
would first look at all PropertyVariants with `lang=de`. There is only one, `description:lang=de:format=date`, but it
has the wrong format so the Publisher will try again with the default language, aka `lang=`.
The only PropertyVariant without language is `description`, which has the correct format and is selected.

For a second example, lets say the user still has selected the language `de` but the Publisher needs the format `number`
to be able to show the property.
In this case the first step is still the same, but the second step where we look at the PropertyVariant `description`
now also fails because it has the wrong format.
Now in the third step the Publisher will ignore the language Variant and will look at all PropertyVariants to find a
valid one. In this
example there is only one so it will select `description:lang=en:format=number`. If there would be more than one valid
Variant with the same format priority, it will sort them alphabetically and select the first one. (Like
the [KeyFilters](#keyfilters) specify.)

## Variants and the iCal-Publisher

Similar to the HTML-Publisher, only that it cannot render Markdown and will ignore this format.

# Known Variants

## Language Variant

Has the VariantName "lang" and the value is a two-letter language abbreviation, like "en", "de", "fr", ... 
Valid values are [IETF language subtags](https://en.wikipedia.org/wiki/IETF_language_tag).

Denotes the language the value is in. Without a language variant it is assumed that this is the "default" language and
will be used as the preferred fallback if the wanted language is not available.

## Format Variant

Has the VariantName "format" and specifies what kind of data and in which format the Value is.

Boudicca currently supports those formats:

* Text (the default when no format is given, please do NOT use any VariantValue for this)
* Numbers (VariantValue "number")
* Dates (VariantValue "date")
* Lists (VariantValue "list")
* Markdown (VariantValue "markdown")

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

Sorting: By numerical value

Searching: Works with Equals queries. Numbers need to be equal. TODO we probably need greater and smaller-queries here.

### Dates

Format: The format is the "Date and time with the offset" format of
the [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) standard. Currently Local Dates (without timezone information) are not allowed.

Examples:

* 2024-04-27T23:59:00+02:00
* 2024-04-27T11:00:00Z

Sorting: Chronologically.

Searching: Works with Equals, After, Before and Duration queries.

### Lists

Format: The format is a comma (`,`) seperated list of values. That means that commas occurring in a value have to be
escaped by a backslash "\" and occurring backslashes also have to be escaped by a backslash.

Examples:

* `value1` (this is a list consisting only of one value "value1")
* `value1,value2` (this is a list consisting of two values "value1" and "value2")
* `val\\ue1,val\,ue2` (this is a list consisting of two values with escaped characters in them "val\\ue1" and "
  val,ue2")
* `value1,,value2` (this is a list consisting of three values "value1", "" (an empty strimg) and "value2")
* `value1, value2` (this is a list consisting of two values "value1" and " value2" (please note the preceding whitespace
  in the second value))
* ` ` (the empty string -> a list with a single empty value)
* `,` (this is a list consisting of two empty values)

Please note that there is no way to specify an empty list, you need to remove the PropertyVariant for this.

Sorting: Alphabetically, case-insensitive (so `aAbB` instead of `abAB`). WARNING: This is like text and is most likely
not what you want, do not rely on sorting lists.

Searching: Works with Equals and Contains queries.

### Markdown

Format: Standard markdown syntax according to [Markdown](https://en.wikipedia.org/wiki/Markdown)

Sorting: Alphabetically, case-insensitive (so `aAbB` instead of `abAB`)

Searching: Works with Equals and Contains queries

Knowledge about markdown is important for rendering it correctly, otherwise it is treated like text.
