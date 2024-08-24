# Semantic Conventions

While most of the core Boudicca Software is very generic (see our [Data Model](DATA_MODEL.md)),
EventCollectors and most Publishers are specialized to work with Events,
and this document describes how Events should look like for them to be interoperable.

### Data types

We define some custom data types we need our properties to be in. The values in () will denote the
underlying [Format Variants](DATA_MODEL.md#format-variant) which are applicable for this data type, ordered by their
priority.
Earlier Format Variants are selected over later ones.

* `name (format=text)`: datatype for the event name, since that one has to be text
* `text (format=markdown,text)`: a text or markdown for structured text, to describe some property
* `date (format=date)`: [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) Timestamp as text, for
  example: `2009-06-30T18:30:00+02:00`
* `number (format=number)`: A number, for example `1`,`0.5`,`-2`
* `url (format=text)`: A URL as text
* `list (format=list)`: A list of text elements
* `enum<?> (format=text)`: Has to be one of the specified distinct values
* `boolean (format=text)`: The text "true" or "false"

Note: publishers have to be able to handle invalid values by for example simply ignoring them.

### Property List

**Events have only two required properties: `name` and `startDate`**

| PropertyName                                   | Meaning                                                                                                                         | [Data Type](#data-types) | [Usage](#usage-explanation) |
|------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|--------------------------|-----------------------------|
| name                                           | The name of the event                                                                                                           | name                     | MANDATORY                   |
| startDate                                      | Time of start for the event                                                                                                     | date                     | MANDATORY                   |
| endDate                                        | Time of end for the event                                                                                                       | date                     | OPTIONAL                    |
| url                                            | A link to the specific source page for this event. If the user wants to see more details about the event, he should find them here | url                      | RECOMMENDED                 |
| [type](#type-property)                         | The type of event, for example `concert`, `????` more examples please                                                           | text                     | RECOMMENDED                 |           
| [category](#category-enum-values)              | The category of an event, for example `MUSIC`, `ART` or `TECH`                     | enum\<EventCategory>     | RECOMMENDED                 |
| description                                    | Text describing this event                                                                                                      | text                     | RECOMMENDED                 |
| [tags](#tags-property)                         | A list of generic tags.                                                                                                         | list                     | RECOMMENDED                 |
| [registration](#registration-enum-values)      | If this is a free event, a event which requires registration or a event which requires a paid ticket                            | enum\<Registration>      | OPTIONAL                    |
| pictureUrl                                     | Url to a picture to be shown                                                                                                    | url                      | RECOMMENDED                 |
| pictureAltText                                 | Alt text for the picture                                                                                                        | text                     | RECOMMENDED                 |
| pictureCopyright                               | Copyright attribution to be shown                                                                                               | text                     | OPTIONAL                    |
| collectorName                                  | Name of the collector which collected this event                                                                                | text                     | OPTIONAL                    |
| sources                                        | A list of all sources that were used to gather info for this event, line by line. This should include all URLs or other sources used. | list                     | RECOMMENDED                 |
| additionalEventsFromSourceUrl                  | an url to page on the event source website where other events can be found (e.g. Termine or Veranstaltungen pages) (if available) | url                      | OPTIONAL                    |
| location.name                                  | The name of the location the event is held in                                                                                   | text                     | RECOMMENDED                 |
| location.url                                   | A link to the website of the location                                                                                           | url                      | RECOMMENDED                 |
| location.latitude                              | Map-coordinates latitude for the location                                                                                       | number                   | OPTIONAL                    |
| location.longitude                             | Map-coordinates longitude for the location                                                                                      | number                   | OPTIONAL                    |
| location.city                                  | The city of the event                                                                                                           | text                     | OPTIONAL                    |
| location.address                               | Full address line for the location                                                                                              | text                     | OPTIONAL                    |
| [recurrence.type](#recurrenceType-enum-values) | Describing how often an event happens. Once, rarely or very often.                                                              | enum\<RecurrenceType>    | RECOMMENDED                 |
| recurrence.interval                            | Describing the interval with which the event recurs                                                                             | text                     | OPTIONAL                    |
| accessibility.accessibleEntry                  | If the entry/exit is accessible                                                                                                 | boolean                  | OPTIONAL                    |
| accessibility.accessibleSeats                  | If there are wheelchair places available on the event hall                                                                      | boolean                  | OPTIONAL                    |
| accessibility.accessibleToilets                | If there are accessible toilets available                                                                                       | boolean                  | OPTIONAL                    |
| accessibility.accessibleAktivpassLinz          | If the Aktivpass Linz may be applicable                                                                                         | boolean                  | OPTIONAL                    |
| accessibility.accessibleKulturpass             | If the Kulturpass may be applicable                                                                                             | boolean                  | OPTIONAL                    |
| concert.genre                                  | Genre of the concert                                                                                                            | text                     | OPTIONAL                    |
| concert.bandlist                               | List of all bands playing                                                                                                       | list                     | OPTIONAL                    |

#### Usage explanation

`MANDATORY`: This field is required for proper operation of boudicca.
`RECOMMENDED`: This field is not required, but it is one of the core features enabling event sorting and filtering for
publishers.
`OPTIONAL`: This field can be used if the information is available.
`DEPRECATED`: This field should no longer be used.

#### Registration enum values

* `free`: a free event which neither requires registration nor a ticket
* `registration`: an event which requires a free registration
* `pre-sales-only`: an event which can only be entered when buying a ticket in advance
* `ticket`: a paid event which requires a ticket

#### "type" property

type is an open field, so any value is permitted, but this is a list of common/known types:

- concert
- theatre
- meetup
- museum
- museum_railway
- model_railway
- guided_tour
- workshop
- ...

#### "tags" property

tags is an open field, so any value is permitted, but this is a list of common/known tags:

- ...
- TODO
- 
#### Category enum values

* `MUSIC`: concerts or other events where the main focus is music
* `TECH`: event with technology as the focus
* `ART`: art exhibitions, comedy, theater, ...
* `SPORT`: everything to do with sports, either watching them or actively participate

#### RecurrenceType enum values

* `REGULARLY`: events that happen about once a week or more often over the period of multiple months
* `RARELY`: events that occur about once a month throughout the year
* `ONCE`: events that occur once a year or more rarely (e.g. Christmas/Easter specials). When the same event is repeated
  e.g. 3 times (for example so that more people can register), but only once a year, it should still be tagged as ONCE.

Examples:

* There is a special event with oldtimers on a museum railway that takes 2 days, but only happens once per year.
    * should be tagged as ONCE
* There is a special event with oldtimers on a museum railway that takes 2 days, but only happens once per year.
    * should be tagged as ONCE
* during Summer from June to September every Thursday there is a special train
    * should be tagged as REGULARLY
* a specific meetup happens about once per month or every two months
    * should be tagged as RARELY

### Internal Properties

The property name prefix `internal.*` is reserved by Boudicca for internal properties needed and will be silently
discarded if ingested.
