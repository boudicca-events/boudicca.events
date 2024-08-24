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

Certain parts of Boudicca need to know about the format of the value, for example the Search Service for searching, or Publishers for rendering a value correctly.

### Possible Solutions

#### Predetermine formats for keys

Also called having a schema, basically means we have a pre-defined format for every key/event property.

Pros:
* We do not have to transport any information otherwise
* Easy to implement and maintain

Cons:
* Not flexible, users cannot introduce their own properties with other formats then the default text

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

## Formats/Structure

Now that we decided how and where to put our variant information we also need to decide on which variants we support right now.

For now, we will support the two variants: Format and Language

### Formats

We decided to support following formats for now:
* Text
* Numbers
* Dates
* Lists
* Markdown

### Language

The second variant we will support is language, which allows to have translations of keys.
