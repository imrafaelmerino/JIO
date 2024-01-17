** Version 1.1.0 **

Breaking changes:

- `PropBuilder` implements `Supplier<Property>` and `build` method becomes `get`
- `StubBuilder` implements `Supplier<IO>` and `build` method becomes `get`
- `CRUDPropBuilder` implements `Supplier<PropBuilder>` and `build` method becomes `get`
- `CRDPropBuilder` implements `Supplier<PropBuilder>` and `build` method becomes `get`

New:

- `JdbcTestDebugger` to debug events from jio-jdbc
- All debuggers assert that they received the appropriate event
