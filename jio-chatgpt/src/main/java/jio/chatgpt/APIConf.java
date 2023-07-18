package jio.chatgpt;

import java.util.Objects;

public class APIConf {


    public final byte[] authHeader;
    public final String version;
    public final String host;


    public APIConf(byte[] authHeader, String version, String host) {
        this.authHeader = Objects.requireNonNull(authHeader);
        if(authHeader.length == 0) throw new IllegalArgumentException("authHeader empty");
        this.version = Objects.requireNonNull(version);
        this.host = Objects.requireNonNull(host);
    }


    @Override
    public String toString() {
        return "APIConf{" +
                "authHeader=*****"  +
                ", version='" + version + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}
