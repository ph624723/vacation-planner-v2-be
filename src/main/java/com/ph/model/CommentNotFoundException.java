package com.ph.model;

public class CommentNotFoundException extends Exception{

    public CommentNotFoundException(Long id){
        super("No comment was found for id: "+id);
    }
}
