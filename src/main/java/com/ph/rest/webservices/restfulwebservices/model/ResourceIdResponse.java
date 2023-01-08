package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

public class ResourceIdResponse<IDtype> extends Response<IDtype>{

    @Getter
    @Setter
    private IDtype resourceId;
}
