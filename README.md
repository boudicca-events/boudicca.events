# boudicca.events TEST

Welcome to the boudicca.events git repo!

boudicca.events collects and shows events from as many sources as possible and displays them on one site, enriched with
Accessibility Information.
To do this we automatically collect the data from different sites with so-called `EventCollectors`.

You can access boudicca.events at https://boudicca.events/ and you can learn more about us
here: https://boudicca.events/about

## Data Model

boudicca.events is only concerned about Events, 
but some of the underlying services are more generic and could be used for other information as well, those would be called "Entry".  
boudicca.events is a database of Events, which are simple Key->Value pairs, but in order to support some use-cases it also
supports format and language Variants of properties, please see [Data Model](docs/DATA_MODEL.md) for more information.
In theory, you can put whatever you want into an Event, but to have some consistency and interoperability it is encouraged
to comply to the keys and formats defined in the [Semantic Conventions](docs/SEMANTIC_CONVENTIONS.md) as well as you can.

## Using Boudiccas REST Apis

When you want to use our data via our REST Apis or want to create our own publisher or service with Boudicca
see [REST](docs/REST.md)

## Architecture

see our [Architecure](docs/architecture/ARCH.md) overview

## Deploying Boudicca

TODO

## Local development setup with IntelliJ

If you want to help us with developing Boudicca or fix a bug, see our [Developing](docs/DEV.md) guide

## Technical descriptions of our Services

If you need more in-depth understanding of our services and their code please see: [Tech](docs/tech/TECH.md)   
