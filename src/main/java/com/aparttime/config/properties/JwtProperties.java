package com.aparttime.config.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private final String secret;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final long secondaryTokenExpiration;

    public JwtProperties(
        String secret,
        long accessTokenExpiration,
        long refreshTokenExpiration,
        long secondaryTokenExpiration
    ) {
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.secondaryTokenExpiration = secondaryTokenExpiration;
    }
}
