# Semantic Conventions

This document explains which properties Boudicca understands, what their meaning is, and what format the values should
be
in. Events can have other properties which will be searchable as text, but will not get special treatment from
Boudicca.

### General encodings

For Boudicca each event is a collection of key-value pairs, for some of those we have defined a special meaning.
Normally when you talk with Boudicca you do that via our REST-API, where each event is encoded into a JSON-object. The
whole JSON-document should be encoded in UTF-8.

### Data types

We use certain data types for the properties we expect.

* `text`: just a simple text/string
* `date`: [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) Timestamp as text, for example: `2009-06-30T18:30:00+02:00`
* `url`: A URL as text
* `coordinates`: longitude + latitude in Decimal degrees (DD) in the format `<latitude>, <longitute>`
* `list<?>`: A list of elements, the `?` describes the type of the elements in the list. Currently, elements in a list
  are seperated by a newline, but this will probably change sometime
* `enum<?>`: Has to be one of the specified distinct values
  * Note: publishers have to be able to handle also a value that was not specified
* `boolean`: The text "true" or "false"

### General Properties

**Events have only two required properties: `name` and `startDate`**

| Key                           | Meaning                                                                                                                               | Format                               | Usage       |
|-------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------|-------------|
| name                          | The name of the event                                                                                                                 | text                                 | MANDATORY   |
| startDate                     | Time of start for the event                                                                                                           | date                                 | MANDATORY   |
| endDate                       | Time of end for the event                                                                                                             | date                                 | OPTIONAL    |
| url                           | A link to the specific source page for this event. If the user wants to see more details about the event, he should find them here    | url                                  | RECOMMENDED |
| type                          | The type of event, for example `concert`, `????` more examples please                                                                 | text ??? maybe enum would be better? | RECOMMENDED |           
| category                      | The category of an event, for example `MUSIC`, `ART` or `TECH`, see EventCategory enum                                                | enum\<EventCategory>                 | RECOMMENDED |
| description                   | Text describing this event                                                                                                            | text                                 | RECOMMENDED |
| description.markdown          | Text describing this event, but formatted with markdown.                                                                              | text                                 | OPTIONAL    |
| recurrence.type               | Describing how often an event happens. Once, rarely or very often.                                                                    | enum\<RecurrenceType>                | RECOMMENDED |
| recurrence.interval           | Describing the interval with which the event recurs                                                                                   | text                                 | OPTIONAL    |
| tags                          | A list of tags. TODO how to describe?                                                                                                 | list\<text>                          | RECOMMENDED |
| registration                  | If this is a free event, a event which requires registration or a event which requires a paid ticket                                  | enum\<Registration>                  | OPTIONAL    |
| pictureUrl                    | Url to a picture to be shown                                                                                                          | url                                  | RECOMMENDED |
| pictureAltText                | Alt text for the picture                                                                                                              | text                                 | RECOMMENDED |
| pictureCopyright              | Copyright attribution to be shown                                                                                                     | text                                 | OPTIONAL    |
| collectorName                 | Name of the collector which collected this event                                                                                      | text                                 | OPTIONAL    |
| sources                       | A list of all sources that were used to gather info for this event, line by line. This should include all URLs or other sources used. | list\<text>                          | RECOMMENDED |
| additionalEventsFromSourceUrl | an url to page on the event source website where other events can be found (e.g. Termine or Veranstaltungen pages) (if available)     | url                                  | OPTIONAL    |

#### Usage explanation

`MANDATORY`: This field is required for proper operation of boudicca.
`RECOMMENDED`: This field is not required, but it is one of the core features enabling event sorting and filtering for
publishers.
`OPTIONAL`: This field can be used if the information is available.
`DEPRECATED`: This field should no longer be used.
| recurrence.type     | Describing how often an event happens. Once, rarely or very often.                                   | enum\<RecurrenceType>                |
| recurrence.interval | Describing the interval with which the event recurs                                                  | text                                 |

#### Registration enum values

* `free`: a free event which neither requires registration nor a ticket
* `registration`: an event which requires a free registration
* `pre-sales-only`: an event which can only be entered when buying a ticket in advance
* `ticket`: a paid event which requires a ticket

#### type

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

#### tags values

tags is an open field, so any value is permitted, but this is a list of common/known tags:

- ...
- TODO

### Location Properties

| Key                  | Meaning                                       | Format      |
|----------------------|-----------------------------------------------|-------------|
| location.name        | The name of the location the event is held in | text        |
| location.url         | A link to the website of the location         | url         |
| location.coordinates | Map-coordinates for the location              | coordinates |
| location.city        | The city of the event                         | text        |
| location.address     | Full address line for the location            | text        |

### Accessibility Properties

| Key                                   | Meaning                                                    | Format  |
|---------------------------------------|------------------------------------------------------------|---------|
| accessibility.accessibleEntry         | If the entry/exit is accessible                            | boolean |
| accessibility.accessibleSeats         | If there are wheelchair places available on the event hall | boolean |
| accessibility.accessibleToilets       | If there are accessible toilets available                  | boolean |
| accessibility.accessibleAktivpassLinz | If the Aktivpass Linz may be applicable                    | boolean |
| accessibility.accessibleKulturpass    | If the Kulturpass may be applicable                        | boolean |

### Concert(/Music?) Properties

| Key              | Meaning                   | Format     |
|------------------|---------------------------|------------|
| concert.genre    | Genre of the concert      | text       |
| concert.bandlist | List of all bands playing | list<text> |

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
discarded if sent by an EventCollector.
