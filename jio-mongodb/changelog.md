** Version 1.1.0 **

Breaking changes:

- `DatabaseBuilder` implements `Supplier<MongoDatabase>` and `build` method becomes `get`
- `CollectionBuilder` implements `Supplier<MongoCollection>` and `build` method becomes `get`
- `ClientSessionBuilder` implements `Supplier<ClientSession>` and `build` method becomes `get`

New:

- `MongoClientEventFormatter` to format JFR events into strings

** Version 1.1.1 **

New:

- JFR failure events find the ultimate cause of exceptions which gives more information about what
  happened
- `MongoClientEventFormatter` now has a singleton

** Version 1.2.0 **

- Upgrade dependencies

** Version 2.0.0 **

Breaking:

- This version only support Java 21 or greater
- All the operations use virtual-threads and no executor can be specified

** Version 2.0.8 **

- JFR event are committed if `shouldCommit` is true
