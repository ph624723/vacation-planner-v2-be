package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

public class AuthTokenResponse extends Response<AuthToken>{

    @Getter
    @Setter
    private AuthToken authToken;
}
