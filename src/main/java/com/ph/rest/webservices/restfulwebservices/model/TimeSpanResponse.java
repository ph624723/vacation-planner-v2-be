package com.ph.rest.webservices.restfulwebservices.model;

import com.ph.model.TimeSpan;
import lombok.Getter;
import lombok.Setter;

public class TimeSpanResponse extends Response<TimeSpan>{

    @Getter
    @Setter
    private TimeSpan timeSpan;
}
