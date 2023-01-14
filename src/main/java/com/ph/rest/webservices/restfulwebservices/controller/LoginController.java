package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.model.UserEntity;
import com.ph.persistence.repository.UserJpaRepository;
import com.ph.rest.webservices.restfulwebservices.model.LoginCredentials;
import com.ph.rest.webservices.restfulwebservices.model.RegisterCredentials;
import com.ph.service.AuthService;
import com.ph.service.EmailServiceImpl;
import com.ph.service.HashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.time.LocalTime;

@Controller
public class LoginController {

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    EmailServiceImpl emailService;

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
            credentials.setErrorText("Username has to have at least 4 characters");
            model.addObject("credentials", credentials);
            return model;
        }
        if(userJpaRepository.existsById(credentials.getUsername())){
            ModelAndView model = new ModelAndView("Generic/register");
            credentials.setErrorText("Username already in use");
            model.addObject("credentials", credentials);
            return model;
        }
        try {
            System.out.println(emailService.sendSimpleMail(credentials.getContact(), "Vacation Planner - User Registration",
                    "Thanks for signing up to the Vacation Planner.\n\n" +
                            "Your initial login credentials will be sent in a seperate e-mail shortly."));
        }catch (Exception e){
            ModelAndView model = new ModelAndView("Generic/register");
            credentials.setErrorText("Confirmation e-mail could not be sent. Please check contact field.");
            model.addObject("credentials", credentials);
            return model;
        }
        PersonEntity personEntity = new PersonEntity();
        personEntity.setContact(credentials.getContact());
        personEntity.setName(credentials.getPersonName());
        UserEntity userEntity = new UserEntity();
        userEntity.setName(credentials.getUsername());
        String passwordPlain = AuthService.generatePassword(credentials.getUsername());
        userEntity.setPassword(HashService.MD5(passwordPlain));
        userEntity.setPersonData(personEntity);
        userEntity = userJpaRepository.save(userEntity);
        try {
            System.out.println(emailService.sendSimpleMail(credentials.getContact(), "Vacation Planner - Initial Credentials",
                    "Thanks for signing up to the Vacation Planner.\n\n" +
                            "You can login using the following credentials:\n" +
                            "Username: " + userEntity.getName() + "\n" +
                            "Password: " + passwordPlain + "\n\n" +
                            "Please change the initial password after your first login."));
        }catch (Exception e){

        }
        ModelAndView model = new ModelAndView("Generic/confirmRegister");

        return model;
    }
}
