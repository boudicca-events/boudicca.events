# Query Doc

## Syntax

Every query is exactly one `expression`, where expressions can take multiple (potentially nested) forms:

| Expression Name | Meaning                                                                           | Format                                                    |
|-----------------|-----------------------------------------------------------------------------------|-----------------------------------------------------------|
| Matches         | If a field exactly matches a text value                                           | `<fieldname> = <text>`                                    |
| Contains        | If a field contains the text value                                                | `<fieldname> ~= <text>`                                   |
| And             | Both child-expressions have to be true so that the whole expression is true       | `<expression> & <expression>`                             |
| Or              | At least one child-expression has to be true so that the whole expression is true | <code>&lt;expression&gt; &#124; &lt;expression&gt;</code> |
| Not             | The child-expression has to be false so that the whole expression is true         | `! <expression>`                                          |
| Grouping        | Marker to identify which expression is the "innermost" TODO better describe       | `( <expression> )`                                        |
