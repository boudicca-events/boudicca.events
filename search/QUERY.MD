# Query Doc

## Syntax

Every query is exactly one `expression`, where expressions can take multiple (potentially nested) forms:

| Expression Name | Meaning                                                                           | Format                          |
|-----------------|-----------------------------------------------------------------------------------|---------------------------------|
| Equals          | If a field exactly equals the text value                                          | `<fieldname> equals <text>`     |
| Contains        | If a field contains the text value                                                | `<fieldname> contains <text>`   |
| And             | Both child-expressions have to be true so that the whole expression is true       | `<expression> and <expression>` |
| Or              | At least one child-expression has to be true so that the whole expression is true | `<expression> or <expression>`  |
| Not             | The child-expression has to be false so that the whole expression is true         | `not <expression>`              |
| Grouping        | Marker to identify how expression should be grouped                               | `( <expression> )`              |

where

* `<text>` is an arbitrary string, but if that string matches a keyword or contains special characters (
  space, (, ), \, ", ...) it has to be surrounded by quotes. Quotes and backslashes in the text have to be escaped by a
  preceding backslash.
* `<fieldname>` is a `<text>` matching a field of an event to be queried. There is the special field `*` which means "
  any field"