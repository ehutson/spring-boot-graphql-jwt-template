package dev.ehutson.template.security.config.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;


@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
    private boolean secure = true;
    private boolean httpOnly = true;
    private long accessTokenExpirationSeconds = 3600;
    private long refreshTokenExpirationSeconds = 86400;
    private String sameSite = "Strict";
    private String path = "/";
    private String accessTokenCookieName = "access_token";
    private String refreshTokenCookieName = "refresh_token";
}