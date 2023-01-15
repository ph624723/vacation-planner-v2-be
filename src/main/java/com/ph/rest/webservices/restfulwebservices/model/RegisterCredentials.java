package com.ph.rest.webservices.restfulwebservices.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

public class RegisterCredentials {
    @Getter
    @Setter
    @Size(min = 4)
    @ApiParam(value = "name for the created account, used for login.", required = true)
    private String username;
    @Getter
    @Setter
    @ApiParam(value = "name for the created public person, used to attach to absences and events.", required = true)
    private String personName;
    @Getter
    @Setter
    @ApiParam(value = "Contact email address used for e.g. initial credentials.", required = true)
    private String contact;
    @Getter
    @Setter
    @JsonIgnore
    private String errorText;

    public RegisterCredentials(){
        errorText = null;
    }
}
