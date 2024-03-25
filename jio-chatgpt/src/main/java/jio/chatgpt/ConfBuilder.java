package jio.chatgpt;

import java.util.Objects;

/**
 * A builder class for configuring the API client's settings.
 */
public final class ConfBuilder {
    private final String authHeader;
    private String version = "v1";
    private String host = "api.openai.com";


    private ConfBuilder(String authHeader) {
        this.authHeader = Objects.requireNonNull(authHeader);
    }

    /**
     * Constructs a new ConfBuilder with the provided authentication header.
     *
     * @param authHeader The authentication header containing the API key or token.
     */
    public static ConfBuilder of(String authHeader) {
        return new ConfBuilder(authHeader);
    }


    /**
     * Sets the version of the API to be used.
     *
     * @param version The version of the API. Defaults to "v1".
     * @return This builder instance for method chaining.
     */
    public ConfBuilder withVersion(String version) {
        this.version = Objects.requireNonNull(version);
        return this;
    }

    /**
     * Sets the host of the API server.
     *
     * @param host The host of the API. Defaults to "api.openai.com".
     * @return This builder instance for method chaining.
     */
    public ConfBuilder withHost(String host) {
        this.host = Objects.requireNonNull(host);
        return this;
    }

    /**
     * Builds and returns an API configuration with the specified settings.
     *
     * @return An APIConf instance representing the configured API client settings.
     */
    public APIConf build() {
        return new APIConf(authHeader, version, host);
    }
}