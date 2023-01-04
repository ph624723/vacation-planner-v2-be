package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ListResponse<T> extends Response<T>{

    @Getter
    @Setter
    private List<T> list;
}
