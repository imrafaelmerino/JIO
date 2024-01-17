** Version 1.1.0 **

Breaking changes:

- `JioHttpClientBuilder` implements `Supplier<JioHttpClient>` and `build` method becomes `get`
- `ClientCredsBuilder` implements `Supplier<OauthHttpClient>` and `build` method becomes `get`

New:

- `HttpClientEventFormatter` to format JFR events into strings

