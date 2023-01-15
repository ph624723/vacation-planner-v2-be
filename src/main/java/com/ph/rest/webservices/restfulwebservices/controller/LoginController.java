package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.model.EmailFailedException;
import com.ph.model.UserNameInUseException;
import com.ph.persistence.repository.UserJpaRepository;
import com.ph.rest.webservices.restfulwebservices.Service.UserService;
import com.ph.rest.webservices.restfulwebservices.model.LoginCredentials;
import com.ph.rest.webservices.restfulwebservices.model.RegisterCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@Controller
@ApiIgnore
public class LoginController {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private UserService userService;

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
            credentials.setErrorText("Wrong username or password");
        }
        if(logout != null){
            credentials.setLogout(true);
        }

        model.addObject("credentials", credentials);

        return model;
    }

    @GetMapping(value = "/register")
    public ModelAndView registerView(){
        ModelAndView model = new ModelAndView("Generic/register");

        RegisterCredentials credentials = new RegisterCredentials();

        model.addObject("credentials", credentials);

        return model;
    }

    @PostMapping(value = "/register")
    public ModelAndView registerConfirm(@Valid @ModelAttribute
                                            RegisterCredentials credentials,
                                        BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            ModelAndView model = new ModelAndView("Generic/register");
            credentials.setErrorText("Username has to have at least 3 characters");
            model.addObject("credentials", credentials);
            return model;
        }
        try {
            userService.registerUser(credentials);
        }catch (EmailFailedException e){
            ModelAndView model = new ModelAndView("Generic/register");
            credentials.setErrorText("Confirmation e-mail could not be sent. Please check contact field.");
            model.addObject("credentials", credentials);
            return model;
        }catch (UserNameInUseException e){
            ModelAndView model = new ModelAndView("Generic/register");
            credentials.setErrorText("Username already in use");
            model.addObject("credentials", credentials);
            return model;
        }
        ModelAndView model = new ModelAndView("Generic/confirmRegister");

        return model;
    }
}
