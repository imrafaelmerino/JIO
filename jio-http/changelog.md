** Version 1.1.0 **

Breaking changes:

- `JioHttpClientBuilder` implements `Supplier<JioHttpClient>` and `build` method becomes `get`
- `ClientCredsBuilder` implements `Supplier<OauthHttpClient>` and `build` method becomes `get`

New:

- `HttpClientEventFormatter` to format JFR events into strings

** Version 1.1.1 **

New:
- JFR failure events find the ultimate cause of exceptions which gives more information about what happened
- `HttpClientEventFormatter` now has a singleton instance 