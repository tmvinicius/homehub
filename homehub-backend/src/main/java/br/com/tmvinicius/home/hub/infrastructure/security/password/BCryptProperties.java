package br.com.tmvinicius.home.hub.infrastructure.security.password;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.password.bcrypt")
public record BCryptProperties(int strength) {
}
