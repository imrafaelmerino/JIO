** Version 1.1.0 **

Breaking changes:

- `JioHttpClientBuilder` implements `Supplier<JioHttpClient>` and `build` method becomes `get`
- `ClientCredsBuilder` implements `Supplier<OauthHttpClient>` and `build` method becomes `get`

New:

- `HttpClientEventFormatter` to format JFR events into strings

** Version 1.1.1 **

New:

- JFR failure events find the ultimate cause of exceptions which gives more information about what
  happened
- `HttpClientEventFormatter` now has a singleton instance

** Version 1.1.2 **

Bugs:

- `HttpClientEventFormatter` missing a comma the format of errors

** Version 1.2.0 ** Breaking:

- `HttpClientEventFormatter` prints time in human readable way instead of milliseconds and other
  changes in the line format (see javadoc)
- JFR event annotations renamed: name from `jio.httpclient` to `jio.http.client.HttpReq`

Refactor:

- `HttpClientEventFormatter` refactor
- rename `ReqEvent` to `HttpReqEvent`

** Version 1.3.0 **

- Print out leaked has been removed

** Version 2.0.0 **

Breaking:

- This version only support Java 21 or greater
- Rename from `ClientCredsBuilder` to `ClientCredentialsBuilder`

New:

- All the requests are made by virtual-threads using the `client.sync(...)` method from the java
  http-client

** Version 2.0.8 **

- JFR event are committed if `shouldCommit` is true
- JioHttpClient implements `Autocloseable`
- Added `shutdonw`, `shutdownNow` to JioHttpClient
- Added event start time into `HttpClientReqEventFormatter`
