package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

public class LoginCredentials{
    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private String newPassword;
    @Getter
    @Setter
    private String newPassword2;
    @Getter
    @Setter
    private Boolean wrongPassword;
    @Getter
    @Setter
    private Boolean unequalPassword;

    public LoginCredentials(){
        wrongPassword = false;
    }
}
