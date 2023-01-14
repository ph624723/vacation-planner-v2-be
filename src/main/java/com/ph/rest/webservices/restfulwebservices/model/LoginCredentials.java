package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

public class LoginCredentials{
    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    @Size(min=6, message = "Password should be at least 6 characters")
    private String newPassword;
    @Getter
    @Setter
    private String newPassword2;
    @Getter
    @Setter
    private String errorText;
    @Getter
    @Setter
    private Boolean logout;

    public LoginCredentials(){

        errorText = null;
        logout=false;
    }
}
