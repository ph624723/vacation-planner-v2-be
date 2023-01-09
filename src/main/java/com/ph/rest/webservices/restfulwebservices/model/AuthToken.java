package com.ph.rest.webservices.restfulwebservices.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdAt;

    @Getter
    @Setter
    private String lifetime;
}
