** Version 1.0.1**

New:

- JFR failure events find the ultimate cause of exceptions which gives more information about what
  happened
- `ExpEventFormatter` to format the JFR event recorded by jio-exp

** Version 1.2.0**

Breaking:

- JFR event annotations renamed: name from `jio.exp` to `jio.exp.EvalExp`
- rename `ExpEventFormatter` to `EvalExpEventFormatter`
- `EvalExpEventFormatter`prints time in a human-readable way and not in milliseconds New:
- `jio.exp.time.Fun` class. Method `formatTime `is very useful, and it's used from different modules
- `jio.exp.Fun` is public. . Method `findUltimateCause ` and `findCause`is very useful, and it's
  used from different modules.

Refactor:

- rename `ExpEvent` to `EvalExpEvent` (internal class)

Others:

- `EvalExpEventFormatter` javadoc improved

** Version 2.0.0**

Breaking:

- This version only supports Java 21 or greater
- Since `Delay` is implemented with virtual threads, methods `retryOn` and `repeatOn` has been
  deleted

New:

- `Delay` implemented with virtual threads instead of `DelayedExecutor`

** Version 2.0.1** Breaking:

- Since we have virtual threads, IO methods with suffix on have been deleted
- `result` method now throws an exception in the signature. Honesty first! If the api client prefers
  to be unsafe use the alternative `join` which is Java idiomatic, but it throws
  `CompletionException` instead of it cause like `result` methods do.

** Version 2.0.3** Breaking:

- Creation of `ExceptionFun` and move public methods from `Fun` into this class

New: Creation of new methods in `ExceptionFun` to handle Exceptions

** Version 2.0.8**

- JFR event are committed if `shouldCommit` is true

** Version 3.0.0-RC1**

Bugs:

- `IO.resource`. The resource was closed before invoking the effect returned by the lambda function
