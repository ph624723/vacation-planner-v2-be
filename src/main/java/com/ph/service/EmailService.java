package com.ph.service;


public interface EmailService {

    // Method
    // To send a simple email
    String sendSimpleMail(String to, String subject, String text)throws Exception;
}
