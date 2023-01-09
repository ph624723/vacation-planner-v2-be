package com.ph.rest.webservices.restfulwebservices.model;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "Format to pass user credentials to authentication controller")
public class Credentials {

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;
}
