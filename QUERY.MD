# _insert name here_ Doc

## Introduction

The _insert name here_ is a simple query language for making custom queries/filters where our normal search is
insufficient.
Queries can look like `name contains Bandname or name contains Bandname2`

## Syntax

A query is always UTF-8 encoded text and every query is exactly one `expression`, where expressions can take multiple (
potentially nested) forms. Please note that expressions are case-insensitive:

| Expression Name  | Meaning                                                                                      | Format                          |
|------------------|----------------------------------------------------------------------------------------------|---------------------------------|
| Equals           | If a field exactly (but case-insensitive) equals the text value                              | `<fieldname> equals <text>`     |
| Contains         | If a field contains (case-insensitive) the text value                                        | `<fieldname> contains <text>`   |
| And              | Both child-expressions have to be true so that the whole expression is true                  | `<expression> and <expression>` |
| Or               | At least one child-expression has to be true so that the whole expression is true            | `<expression> or <expression>`  |
| Not              | The child-expression has to be false so that the whole expression is true                    | `not <expression>`              |
| After            | Filter events starting at or after the given date                                            | `after <date>`                  |
| Before           | Filter events starting at or before the given date                                           | `before <date>`                 |
| Is               | Filter events belonging to a certain category                                                | `is <category>`                 |
| Grouping         | Marker to identify how expression should be grouped                                          | `( <expression> )`              |
| Duration Longer  | Filter events on their duration in hours (inclusive), events without endDate have 0 duration | `durationLonger <number`        |
| Duration Shorter | Filter events on their duration in hours (inclusive), events without endDate have 0 duration | `durationShorter <number>`      |

where

* `<text>` is an arbitrary string, but if that string matches a keyword or contains special characters (
  space, (, ), \, ", ...) it has to be surrounded by quotes. Quotes and backslashes in the quote-escaped text have to be
  escaped by a preceding backslash. Special characters which are allowed without quote-escaping are the dot '.' and the
  minus '-'.
  This is because a lot of field-names contain a dot (like `location.name`) and dates contain -, and these exceptions
  makes it easier to write queries for them.
* `<number>` is a integer or decimal value, can be positive or negative, for example `2`, `-5`, `-2.5`
* `<fieldname>` is a `<text>` matching a field of an event to be queried. There is the special field `"*"` which means
  "any field". Please note that * is a special character and thus the quotes are needed. Also note that the fieldname
  matching is case-sensitive, so the fieldnames `name` and `NAME` are different fields.
* `<date>` is a `<text>` in the ISO Local Date format `YYYY-MM-DD`, for example `2023-05-27`
* `<category>` is a `<text>` which value has to be one of `MUSIC`,`TECH`,`ART`,`OTHER`

In contrast to other queries or math there is no operator precedence here, they will be ordered/grouped randomly
(depending solely on the parser). So make sure to use the grouping `(...)` mechanism!

## Examples

* Search for any event containing the text "technology" in the name: `name contains technology`
* Search for any event NOT containing the text "technology" in the name: `not name contains technology`
* Search for any event containing the text "technology" in any field (like when you search on our
  website): `"*" contains technology`
* Search for any event in either Linz or Enns: `location.city equals Linz or location.city equals Enns`
* Search for any event containing `a "longer" sentence` in the
  description: `description contains "a \"longer\" sentence"`
* Search for any event in Wien but not in
  Gasometer: `location.city equals Wien and (not location.name equals Gasometer)`
* Search for any event while I am in Vienna on
  holiday: `location.city equals Wien and after 2023-05-27 and before 2023-05-31`

See our [Semantic Conventions](SEMANTIC_CONVENTIONS.md) to find common field names.

## Architecture of _insert name here_

Using queries is a three-step process: Lexing, Parsing, Evaluation

### Lexing

The Lexer is responsible for taking the query-string and breaking it up into a list of tokens. This means the handling
for escaping text and recognizing keywords happens here.

For example the query `name equals "BigBand Blues"` will result in the three
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

For example the SimpleEvaluator has a collection of Events in form of Map<String,String> (so we do not have to
make special rules for name and startDate) and just iterates over each Event and evaluates the Expression on it.