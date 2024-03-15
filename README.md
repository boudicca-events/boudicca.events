# boudicca.events

Welcome to the boudicca.events git repo!

boudicca.events collects and shows events from as many sources as possible and displays them on one site, enriched with
Accessibility Information.
To do this we automatically collect the data from different sites with so-called `EventCollectors`.

You can access boudicca.events at https://boudicca.events/ and you can learn more about us
here: https://boudicca.events/about

## Data Model

Our data model is really simple, an Event/Entry is simply a collection of key-value pairs. In theory, you can put whatever you want into an Event,
but to have some consistency and interoperability we encourage you to comply to the keys and formats you find in our [Semantic Conventions](SEMANTIC_CONVENTIONS.md) as good as you can. 

## Using Boudiccas REST Apis

When you want to use our data via our REST Apis or want to create our own publisher or service with Boudicca see [REST](docs/REST.md)

## Architecture

see our [Architecure](docs/architecture/ARCH.md) overview

## Deploying Boudicca

TODO

## Local development setup with IntelliJ

If you want to help us with developing Boudicca or fix a bug, see our [Developing](docs/DEV.md) guide
