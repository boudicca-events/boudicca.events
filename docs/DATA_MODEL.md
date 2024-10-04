# Boudicca's Data Model

## Overview

Boudicca is a database containing Entries, which are a collection of `Key`->`Value` Pairs.
One example of such a Pair could be `name`->`My Entry` which has the key "name" and the value "My Entry" and could
represent the name of this Entry.

An `Entry` is just a generic way of talking about the data in Boudicca, because it has no semantic meaning.
Normally you will work with `Events`, which are Entries representing some event.
To make those events interoperable we have standardised fields and their meaning which you can find in
our [Semantic Conventions](SEMANTIC_CONVENTIONS.md) documentation.
But nothing is stopping you from adding your own fields to Entries or Events.

Sometimes you need more information about the value of one Pair, for example which language the value is in, or if
this value is supposed to be a number, text, date, ...
For this Boudicca adds so-called `Variant` information to the `Key`,
for example we can add one `Variant` with the name `lang` and the value `en` to our key,
which specifies that the language of the value is english.

When sending or receiving Entries/Events we need to "flatten" the `Keys` into a single string.
To do this we take the key name and append it with all the variants seperated by `:`.
Variant name and value are seperated by a `=`.
For example with the key "name" which is in english we could have the flat key `name:lang=en`->`My Entry` or
to specify that something is a number we can write `price:format=number`->`20`.

See [Format](#format-variant) and [Language](#language-variant) for more information on those Variants.
You can of course chain and combine those Variants, but be aware that most of the time the same Variant is only allowed
to appear once per Key and watch out for the [canonical form](#canonical-form).

## Definitions

At this point we need to specify some terminology a bit better. Also have a look at the [Examples](#examples) below to
help you understand those definitions better.

* Entry: One generic entry in our database, which consists of multiple `Key`->`Value` pairs
* Event: A specialised `Entry`, which represents an event and has two mandatory keys: `name` and `startDate`.
  See [Semantic Conventions](SEMANTIC_CONVENTIONS.md) for more information.
* FieldName: Name of one `Key` of an `Entry`. Can only consist of letters, numbers and `.`. Can have multiple `Variants`
* Variant: Specifying what kind of Variant (for example what language or what format) this key is. Format
  is `VariantName=VariantValue`
* VariantName: The name of the Variant.
* VariantValue: The value of the Variant. For example, for the Variant Format it should be the format the value is in.
  For Language, it should be the language of the Value.

Currently supported variants are:

* [Language](#language-variant): which language a value is in
* [Format](#format-variant): what kind of data this value is, for example text, dates, ...

If a certain Variant makes sense for a certain field needs to be decided per field and that fields defined
meaning. For example, it does not make sense to have a language variant for `startDate`.

### Examples

Let's look at an Event with following `Key`->`Value` pairs:

* `name` -> `My Event`
* `startDate:format=date` -> `2024-04-27T23:59:00+02:00`
* `description` -> `My Event is awesome!`
* `description:lang=de` -> `Mein Event ist großartig!`
* `description:format=markdown:lang=de` -> `#Mein Event ist großartig!`

The `Key` `startDate:format=date` consists of the `FieldName` `startDate` and one `Variant` `format=date`,
seperated by `:`.

The `Variant` `format=date` consists of the `VariantName` `format` and the `VariantValue` `date`.

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

Empty VariantNames on the other hand are invalid and will be rejected as an error.

If you are using Boudicca's libraries to work with Variants the library will make sure all keys are canonical, and our
EventDB will also make sure that all ingested keys are changed to be canonical. That also means that all keys you get
from our EventDB and our Search Service are guaranteed to be canonical.

## KeyFilters

When you are working with Fields which have multiple Variants or are using the [Boudicca Query Language](QUERY.md)
you often want to select some subset of all available Properties of one Event for querying or displaying.
To help you with this Boudicca offers support for so-called KeyFilters, which look like normal Keys.
One example would be `description:lang=de`, which is a filter filtering for the FieldName `description` and the
Variant `lang=de`.
This is done by taking all available Keys of one Event, taking only those with the
FieldName `description` and then only taking those whose `lang` Variant matches the value `de`. It does not matter if
the Key has additional Variants, those will not be looked at.

If KeyFilters return more than one Keys, the result will be sorted alphabetically by
their FieldName, and then by their first Variant, then their second Variant, and so on.

One difference compared to normal Keys is the empty VariantValue, like `lang=`. This means "select all Keys which
do NOT have the VariantName at all".
Another difference is that `*` is allowed as a FieldName, this would match and select all FieldNames. You can still
add Variant Filters to the `*` filter.
You can also use `*` as a VariantValue, like `lang=*`, which will select all Keys which do have a language
Variant, regardless of what value this Variant has.

Let's look at some examples given following Keys with the FieldName `description` :

* `description`
* `description:lang=de`
* `description:lang=de:format=markdown`
* `description:lang=en`
* `description:lang=en:format=markdown`

With the filter `description:lang=de` you would get the two Keys `description:lang=de`
and `description:lang=de:format=markdown`.

With the filter `description` you would get all Keys as a result, because there is no filter except the FieldName.

With the empty VariantValue filter `description:lang=` you will only get `description` as a result, since this is the
only Key without the `lang` Variant.

## Unknown Variants

Boudicca will ignore all Variants it does not understand and treat the Keys as if those Variants are not here. This
behaviour is suggested for all Software handling Boudicca Keys.

## Variants and the Search Service

The Search Service will evaluate [Queries](QUERY.md) like the `contains` Query by getting all Keys
matching the [KeyFilter](#keyfilters) given to the Query and then performing the Query against all those Keys.
If any match, it will evaluate to true.
The only Variant the Search Service does know about is the [Format Variant](#format-variant), because this will
influence how searching, parsing and sorting works on those Values.

If you create invalid combinations of Queries and Variants (like an `contains` Query and a `format=date` Key) Search
Service will simply ignore those Keys as if they were not present.

So lets say you have the following Keys:

* `description`
* `description:lang=de`
* `description:lang=de:format=date`
* `description:lang=en`
* `description:lang=en:format=date`

For the query `"description" contains "my search text"` Search Service will first select all Keys which
match the given KeyFilter `description`, which are all of the above. Then it will discard all with an invalid format
Variant, which leaves us with the Keys `description`, `description:lang=de` and `description:lang=en`. It
will now evaluate the `contains` query against the value of those three Keys, and if any match the query
will evaluate to true.

## Variants and the HTML-Publisher

For displaying values with different Formats the HTML-Publisher follows
the [Semantic Conventions](SEMANTIC_CONVENTIONS.md) and tries to find the Variant with the highest priority.

If the user has selected a language (feature pending :P ) the Publisher will preferably select a Value with
the selected language.
If none are available (or the user has no language selected) it will fall back to the default language (aka
Keys without any language).
If none are available it will select any Value with a valid Format.

For example, given following Keys:

* `description`
* `description:lang=de:format=date`
* `description:lang=en`
* `description:lang=en:format=number`

If the HTML-Publisher wants to show some value with the format `text` (aka no format Variant) and the user has selected
the language `de`, the HTML-Publisher
would first look at all Keys with `lang=de`. There is only one, `description:lang=de:format=date`, but it
has the wrong format so the HTML-Publisher will try again with the default language, aka `lang=`.
The only Key without language is `description`, which has the correct format and is selected.

For a second example, lets say the user still has selected the language `de` but the HTML-Publisher needs the format
`number` to be able to show the value.
In this case the first step is still the same, but the second step where we look at the Key `description`
now also fails because it has the wrong format.
Now in the third step the HTML-Publisher will ignore the language Variant and will look at all Keys to find one
with a valid format. In this example there is only one, so it will select `description:lang=en:format=number`. If there
would be more than one valid Variant with the same format priority, it will sort them alphabetically and select the
first one. (like the [KeyFilters](#keyfilters) specify.)

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
the [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) standard. Currently Local Dates (without timezone information)
are not allowed.

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

Please note that there is no way to specify an empty list, you need to remove the Value for this.

Sorting: Alphabetically, case-insensitive (so `aAbB` instead of `abAB`). WARNING: This is like text and is most likely
not what you want, do not rely on sorting lists.

Searching: Works with Equals and Contains queries.

### Markdown

Format: Standard markdown syntax according to [Markdown](https://en.wikipedia.org/wiki/Markdown)

Sorting: Alphabetically, case-insensitive (so `aAbB` instead of `abAB`)

Searching: Works with Equals and Contains queries

Knowledge about markdown is important for rendering it correctly, otherwise it is treated like text.
