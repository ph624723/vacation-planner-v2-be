package com.ph.rest.webservices.restfulwebservices.model;

import lombok.Getter;
import lombok.Setter;

public class CommentResponse extends Response<Comment>{

    @Getter
    @Setter
    private Comment comment;
}
