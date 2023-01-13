package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.rest.webservices.restfulwebservices.model.LoginCredentials;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {

    @GetMapping(value = "/login")
    public ModelAndView loginView(
            @RequestParam(required = false)
            String error,
            @RequestParam(required = false)
            String logout
    ){
        ModelAndView model = new ModelAndView("Generic/login");

        LoginCredentials credentials = new LoginCredentials();
        if(error != null){
            credentials.setWrongPassword(true);
        }
        if(logout != null){
            credentials.setLogout(true);
        }

        model.addObject("credentials", credentials);

        return model;
    }
}
