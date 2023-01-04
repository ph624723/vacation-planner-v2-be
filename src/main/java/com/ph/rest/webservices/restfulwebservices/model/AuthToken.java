package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

public class AuthToken {

    @Getter
    @Setter
    private String tokenKey;

    @Getter
    @Setter
    private LocalDateTime createdAt;

    @Getter
    @Setter
    private String lifetime;
}
