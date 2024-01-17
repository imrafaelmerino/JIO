** Version 1.1.0 **

Breaking changes:

- `DatabaseBuilder` implements `Supplier<MongoDatabase>` and `build` method becomes `get`
- `CollectionBuilder` implements `Supplier<MongoCollection>` and `build` method becomes `get`
- `ClientSessionBuilder` implements `Supplier<ClientSession>` and `build` method becomes `get`

New:

- `MongoClientEventFormatter` to format JFR events into strings

