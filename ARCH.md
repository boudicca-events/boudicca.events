# Architecture

There are three big groups of services interacting to make boudicca.events work:

### EventCollectors

The EventCollectors job is to gather event data, enrich them, and then send them to the EventDB.

### The Core

Our core boudicca system currently consists of two services:

1. The EventDB which job it is to accept new event data from collectors, persist them and provide them for other
   services to consume.
2. The Search service is a service which provides search functionality on the EventDB data and is mostly used by the
   publishers.

### Publishers

Publishers are services which make the data of boudicca.events accessible to users. This can take many forms, for
example our website is the so-called html-publisher. There can be other publishers as well, for different publishers for
different formats as ical, RSS, PDF, ... or for different purposes (prefiltered event data for only music, ... for
example)
