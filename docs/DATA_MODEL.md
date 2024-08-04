# Boudicca's Data Model

TODO write intro to this whole thing and add a link to [Semantic Conventions](docs/SEMANTIC_CONVENTIONS.md) somewhere 


At this point we need to introduce some more precise terminology:

* Entry: One generic entry in our database, which consists of multiple "Key"->"Value" mappings, where key and value are strings.
* Event: A specialised "Entry", which represents an event and has two mandatory keys: "name" and "startDate"
* Key: Key value of one "Key"->"Value" mapping of one entry. Can contain multiple variant information. Format is "PropertyName"\{:"VariantInformation"}
* PropertyName: Name of the property of an entry. Can only consist of letters, numbers and "." TODO is that ok? do we want to allow other special chars?
* VariantInformation: Specifying what kind of variant this key is. Format is "VariantName"="VariantValue"
* VariantName: The name of the variant. TODO what to do if unknown value? ignore the value? but if it is the only one?
* VariantValue: The value of the variant. For example, for the variant Format it should be the format the value is in. For Language it should be the language of the Value.
* Value: The value of one property. Has to conform to the specified variant data, so conform to the correct language and/or format.

Currently supported variants are:
* Language: which language a value is in
* Format: what kind of data this value is, for example text, dates, ...

If a certain variant makes sense for a certain property needs to be decided per property basis.

#### Language Variant

Has the VariantName "lang" and the value is a two-letter language abbreviation, like "en", "de", "fr", ...

TODO define what that means for searching, for displaying, how languages are chosen and so on

#### Format Variant

Has the VariantName "format" and for the value see the [Formats](#formats) section.

TODO define more stuff here?


## Formats

We decided to support following formats for now:
* Text (VariantValue "text" as well as the default when no format is given)
* Numbers (VariantValue "number")
* Dates (VariantValue "date")
* Lists (VariantValue "list")
* Markdown (VariantValue "markdown")

### Text

Simply text

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