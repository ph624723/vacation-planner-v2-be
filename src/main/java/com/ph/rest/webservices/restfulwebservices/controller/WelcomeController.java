package com.ph.rest.webservices.restfulwebservices.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
//@RequestMapping("/test")
public class WelcomeController {

    @RequestMapping("/")
    public String welcome(){
        //return new ResponseEntity<String>("Nothing to see here. Go to /swagger-ui.html for documentation of available endpoints.", HttpStatus.OK);
        return "redirect:/swagger-ui.html";
    }
}
