package jio.chatgpt;

import java.util.Objects;

public class ConfBuilder {
    private final byte[] authHeader;
    private String version = "v1";
    private String host = "api.openai.com";

    public ConfBuilder(byte[] authHeader) {
        this.authHeader = Objects.requireNonNull(authHeader);
    }


    /**
     * The version of the API. Defaults to v1
     * @param version the version of the API
     * @return this builder
     */
    public ConfBuilder setVersion(String version) {
        this.version = Objects.requireNonNull(version);
        return this;
    }

    /**
     * The host of the API. Defaults to api.openai.com
     * @param host the host of the API
     * @return this builder
     */
    public ConfBuilder setHost(String host) {
        this.host = Objects.requireNonNull(host);
        return this;
    }


    public APIConf build() {
        return new APIConf(authHeader, version, host);
    }
}