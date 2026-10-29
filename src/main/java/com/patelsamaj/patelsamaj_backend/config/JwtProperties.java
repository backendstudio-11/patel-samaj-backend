package com.patelsamaj.patelsamaj_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private String secret;

    private long expirationMs;

    private long refreshExpirationMs;

    private String cookieName;

    private String cookiePath;

    private boolean cookieSecure;

    private String cookieSameSite;
}
