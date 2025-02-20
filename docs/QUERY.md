# Boudicca Query Language

## Introduction

The Boudicca Query Language is a simple query language for making custom queries/filters where our normal search is
insufficient.
Queries can look like `"name" equals "Bandname" or "description" contains "my event"`

## Syntax

A query is always UTF-8 encoded text and every query is exactly one `expression`, where expressions can take multiple (
potentially nested) forms. Please note that expressions are case-insensitive:

| Expression Name | Meaning                                                                                                                                                                                         | Format                                                                              |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| Equals          | If a property exactly (but case-insensitive) equals the text value. For lists, one of the list entries must exactly (but case-insensitive) match the text value.                                | `<keyFilter> equals <text>`                                                         |
| Contains        | If a property contains (case-insensitive) the text value. For lists, one of the list entries must contain (but case-insensitive) the text value.                                                | `<keyFilter> contains <text>`                                                       |
| And             | Both child-expressions have to be true so that the whole expression is true                                                                                                                     | `<expression> and <expression>`                                                     |
| Or              | At least one child-expression has to be true so that the whole expression is true                                                                                                               | `<expression> or <expression>`                                                      |
| Not             | The child-expression has to be false so that the whole expression is true                                                                                                                       | `not <expression>`                                                                  |
| After           | Filter events starting at or after the given date. Only works with Format Variant `date`                                                                                                        | `<keyFilter> after <date>`                                                          |
| Before          | Filter events starting at or before the given date. Only works with Format Variant `date`                                                                                                       | `<keyFilter> before <date>`                                                         |
| Grouping        | Marker to identify how expression should be grouped                                                                                                                                             | `( <expression> )`                                                                  |
| Duration        | Filter events on their duration in hours (inclusive), events without startDate or endDate have 0 duration. You can filter for longer or shorter duration. Only works with Format Variant `date` | `duration <keyFilter of startDate> <keyFilter of endDate> longer\|shorter <number>` |
| HasField        | Checks that the event has this field (or any Variant) set, and it is not the empty string.                                                                                                      | `hasField <keyFilter>`                                                              |

where

* `<text>` is an arbitrary string surrounded by quotes. Quotes and backslashes in the quote-escaped text have to be
  escaped by a preceding backslash.
* `<number>` is a integer or decimal value, can be positive or negative, for example `2`, `-5`, `-2.5`
* `<date>` is a `<text>` in the ISO Local Date format `"YYYY-MM-DD"`, for example `"2023-05-27"`
* `<keyFilter>` is a `<text>` representing a [Key Filter](DATA_MODEL.md#keyfilters) matching some Properties of an entry to be queried.
  The most simple way is to just specify the PropertyName like `"name"`. You can add filters for different Variants if
  needed like for example `"name:lang=de"` to only query german Properties of the `name` property. There is the special
  PropertyName `"*"` which means "any PropertyName". Please note that PropertyName matching is case-sensitive, so the
  property names `"name"`  and `"NAME"` are different properties. If a Key Filter matches multiple Properties, the
  expression is evaluated against all of them, and if any evaluate to true, the whole expression is true.

The operator precedence is with the lowest starting: `or` -> `and` -> `not`, so not is the strongest binding one.
You can use the grouping `(...)` mechanism to circumvent the order.

## Examples

* Search for any event containing the text "technology" in the name: `"name" contains "technology"`
* Search for any event containing the text "technology" in the description, but only for english
  descriptions: `"description:lang=en" contains "technology"`
* Search for any event NOT containing the text "technology" in the name: `not ("name" contains "technology")` **(parenthesis are not needed in this example but make it clearer)**
* Search for any event containing the text "technology" in any field (like when you search on our
  website): `"*" contains "technology"`
* Search for any event in either Linz or Enns: `"location.city" equals "Linz" or "location.city" equals "Enns"`
* Search for any event containing `a "longer" sentence` in the
  description: `"description" contains "a \"longer\" sentence"`
* Search for any event in Wien but not in
  Gasometer: `"location.city" equals "Wien" and (not "location.name" equals "Gasometer")` **(parenthesis are not needed in this example but make it clearer)**
* Search for any event while I am in Vienna on
  holiday: `"location.city" equals "Wien" and "startDate" after "2023-05-27" and "startDate" before "2023-05-31"`
* Search for events with a duration of 2 hours or less: `duration "startDate" "endDate" shorter 2`
* Search for events which have an english description: `hasField "description:lang=en"`

See our [Semantic Conventions](SEMANTIC_CONVENTIONS.md) to find common property names and their meaning.
