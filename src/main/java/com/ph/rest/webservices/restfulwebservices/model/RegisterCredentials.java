package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

public class RegisterCredentials {
    @Getter
    @Setter
    @Size(min = 4)
    private String username;
    @Getter
    @Setter
    private String personName;
    @Getter
    @Setter
    private String contact;
    @Getter
    @Setter
    private String errorText;

    public RegisterCredentials(){
        errorText = null;
    }
}
