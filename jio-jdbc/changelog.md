** Version 0.0.3**

New:

- JFR failure events find the ultimate cause of exceptions which gives more information about what happened
- `JdbcEventFormatter` now has a singleton
- flag enableJFR defaults to true

** Version 0.0.4**

Bugs:

- `JdbcEventFormatter` reads `counter` instead of `opCounter`

** Version 0.0.5**

New:

- `JdbcLambda` new interface to model any interaction with the database
- `QueryOneStm` and `QueryOneStmBuilder` to query for just one row

** Version 0.0.5**

New: 

- All the builders return `JdbcLambda` and all its implementations (`QueryStm`, `UpdateStm` are hidden)

Internal:
- Refactor `JdbcEventFormatter` and `StmEvent`