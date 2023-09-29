package jio.chatgpt;

import java.util.Objects;

/**
 * Configuration class for storing API-related configuration details.
 */
public final class APIConf {

    /**
     * The authentication header, presumably for API authentication.
     */
    public final byte[] authHeader;

    /**
     * The API version.
     */
    public final String version;

    /**
     * The API host.
     */
    public final String host;

    /**
     * Constructs an API configuration with the provided authentication header, version, and host.
     *
     * @param authHeader The authentication header.
     * @param version    The API version.
     * @param host       The API host.
     */
    public APIConf(byte[] authHeader, String version, String host) {
        this.authHeader = Objects.requireNonNull(authHeader);
        if (authHeader.length == 0) throw new IllegalArgumentException("authHeader empty");
        this.version = Objects.requireNonNull(version);
        this.host = Objects.requireNonNull(host);
    }

    /**
     * Returns a string representation of the API configuration with the authentication header obscured.
     *
     * @return A string representation of the API configuration.
     */
    @Override
    public String toString() {
        return "APIConf{" +
                "authHeader=*****" +
                ", version='" + version + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}
