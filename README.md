# boudicca

## Local development setup with IntelliJ

### Frontend only setup

If you want to develop only on the Frontend ([publisher-html](publisher-html)) without building the backend, then you can use the `Boudicca HTML Publisher` run config.

This run configuration uses the boudicca website as backend.

### Full setup

Build all modules with gradle first.

In the `LocalSearch` service run configuration, set the `BOUDICCA_LOCAL=true` environment variable.

This prevents the `LocalSearch` service from caching the search results.

To run the application, use the `Local Setup (without collectors)`.

Then you can fill up the database with one of two options:

- (recommended) Run `LocalFetchFromOnlineBoudiccaKt` to fetch the events from the boudicca website.
- (slow option): Run `BoudiccaEventCollectorsKt` to scrape all current events from the websites with the help of the event collectors

After the database is filled up, call `localhost:8080` to see the application.