package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

public class UserResponse extends Response<User>{

    @Getter
    @Setter
    private User user;
}
